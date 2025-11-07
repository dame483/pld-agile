package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.SauvegarderTournee;
import fr.insalyon.pldagile.sortie.TourneeUpload;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * État représentant une tournée déjà calculée.
 * Permet de gérer les opérations sur les tournées calculées, telles que
 * la sauvegarde, la création de feuilles de route et le passage en mode modification.
 */
public class EtatTourneeCalcule implements Etat {

    /** La carte du réseau de livraisons */
    private Carte carte;

    /** La demande de livraison associée à la tournée */
    private DemandeDeLivraison demande;

    /** La liste de toutes les tournées calculées */
    private final List<Tournee> toutesLesTournees;

    /**
     * Constructeur de l'état Tournée Calculée.
     *
     * @param carte La carte utilisée pour le calcul des tournées
     * @param demande La demande de livraison
     * @param toutesLesTournees Liste des tournées calculées
     */
    public EtatTourneeCalcule(Carte carte, DemandeDeLivraison demande, List<Tournee> toutesLesTournees) {
        this.carte = carte;
        this.demande = demande;
        this.toutesLesTournees = toutesLesTournees;
    }


    /** {@inheritDoc} */
    @Override
    public Carte chargerCarte(Controleur c, MultipartFile file) throws XMLFormatException {
        Object result = chargerXML("carte", file, null);

        if (result instanceof Carte carte) {
            c.setEtatActuelle(new EtatCarteChargee(carte));
            return carte;
        } else if (result instanceof Exception e) {
            if (e instanceof XMLFormatException xmlEx) {
                throw xmlEx;
            } else {
                throw new XMLFormatException("Erreur lors du chargement de la carte : " + e.getMessage());
            }
        } else {
            throw new XMLFormatException("Fichier XML invalide ou carte non chargée.");
        }
    }


    /** {@inheritDoc} */
    @Override
    public Object chargerDemandeLivraison(Controleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = chargerXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison) {
            c.setEtatActuelle(new EtatDemandeLivraisonChargee(carte, (DemandeDeLivraison) dem));
            return dem;
        }
        return dem;
    }


    /** {@inheritDoc} */
    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        try {
            LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();

            CalculTournees t = new CalculTournees(carte, demande, vitesse, nombreLivreurs, heureDepart);
            List<Tournee> toutesLesTournees = t.calculerTournees();

            c.setEtatActuelle(new EtatTourneeCalcule(carte, demande, toutesLesTournees));
            return toutesLesTournees;

        } catch (Exception e) {
            return e;
        }
    }


    /** {@inheritDoc} */
    @Override
    public Object chargerXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        if (file == null || file.isEmpty()) {
            throw new XMLFormatException("Le fichier est vide ou nul.");
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            System.out.println("Fichier temporaire créé : " + tempFile.getAbsolutePath());
            Object result;

            switch (type.toLowerCase()) {
                case "carte":
                    result = CarteParseurXML.loadFromFile(tempFile);
                    break;

                case "demande":
                    result = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                    break;

                case "tournee":
                    Object tournee = parseurTourneeJson.parseurTournee(tempFile.getAbsolutePath());
                    Object demande = parseurTourneeJson.parseurDemandeDeLivraison(tempFile.getAbsolutePath());
                    result = new TourneeUpload(tournee, demande);
                    break;

                default:
                    throw new XMLFormatException("Type de fichier non reconnu : " + type);
            }

            return result;

        } catch (XMLFormatException e) {
            throw e;

        } catch (Exception e) {
            throw new XMLFormatException("Erreur lors du chargement du fichier XML/JSON : " + e.getMessage(), e);

        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        List<Path> chemins = new ArrayList<>();
        try {
            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee t = toutesLesTournees.get(i);
                FeuilleDeRoute feuille = new FeuilleDeRoute(t);
                Path chemin = feuille.genererFeuilleDeRoute(i);
                chemins.add(chemin);
                System.out.println("Feuille de route créée pour la tournée #" + (i + 1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return chemins;
    }

    @Override
    public Object sauvegarderTournee(Controleur c) {
        try {
            if (toutesLesTournees == null || toutesLesTournees.isEmpty()) {
                return "Aucune tournée à sauvegarder.";
            }
            SauvegarderTournee sauvegarderTournee = new SauvegarderTournee(toutesLesTournees, c.getDemande());
            sauvegarderTournee.sauvegarderTournee();
            System.out.println("Tournées sauvegardées avec succès !");

            return "Toutes les tournées ont été sauvegardées avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }


    /** {@inheritDoc} */
    @Override
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        Object result = chargerXML("tournee", file, carte);

        if (result instanceof Exception e) {
            return e;
        }

        if (!(result instanceof TourneeUpload upload)) {
            return new Exception("Résultat inattendu lors du chargement de la tournée");
        }

        Object tourneeObj = upload.getTournee();
        Object demandeObj = upload.getDemande();

        DemandeDeLivraison demande;
        if (demandeObj instanceof DemandeDeLivraison d) {
            demande = d;
        } else {
            return new Exception("Objet de demande invalide");
        }

        List<Tournee> toutesLesTournees;
        if (tourneeObj instanceof Tournee tournee) {
            toutesLesTournees = List.of(tournee);
        } else if (tourneeObj instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
            toutesLesTournees = (List<Tournee>) liste;
        } else {
            return new Exception("Fichier JSON invalide ou format incorrect");
        }

        c.setEtatActuelle(new EtatTourneeCalcule(carte, demande, toutesLesTournees));

        return new TourneeUpload(toutesLesTournees, demande);
    }


    /** {@inheritDoc} */
    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        if (tournee == null) {
            System.err.println("Erreur : aucune tournée fournie pour passer en mode modification.");
            return;
        }
        c.setEtatActuelle(new EtatModificationTournee(carte, tournee));
    }


    /** {@inheritDoc} */
    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        throw new IllegalStateException("Erreur : aucune modification à sauvegarder à ce stade.");
    }

    public Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse){
        throw new IllegalStateException("Erreur : Pas de modification de tournée possible dans l'état actuel");
    }


    /** {@inheritDoc} */
    @Override
    public String getNom() {
        return "Etat Tournée Calculé";
    }
}
