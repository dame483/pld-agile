package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.TourneeUpload;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Représente l'état de l'application où une carte et une demande de livraison ont été chargées.
 * Permet de calculer les tournées en fonction de la demande, ou de charger de nouvelles cartes ou demandes.
 * Certaines opérations comme la création de feuilles de route ou la sauvegarde sont encore impossibles
 * avant le calcul des tournées.
 */
@Component
public class EtatDemandeLivraisonChargee implements Etat {

    /** Carte actuellement chargée. */
    private final Carte carte;

    /** Demande de livraison actuellement chargée. */
    private final DemandeDeLivraison demande;

    /**
     * Constructeur de l'état avec la carte et la demande déjà chargées.
     *
     * @param carte Carte chargée
     * @param demande Demande de livraison chargée
     */
    public EtatDemandeLivraisonChargee(Carte carte, DemandeDeLivraison demande) {
        this.carte = carte;
        this.demande = demande;
    }

    /**
     * Charge une nouvelle carte et met à jour l'état si réussite.
     */
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

    /**
     * Charge une nouvelle demande de livraison et met à jour l'état si réussite.
     */
    @Override
    public Object chargerDemandeLivraison(Controleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = chargerXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison demande) {
            c.setEtatActuelle(new EtatDemandeLivraisonChargee(this.carte, demande));
            return demande;
        }
        return dem;
    }

    /**
     * Calcule les tournées à partir de la demande et met à jour l'état.
     */
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

    /**
     * Upload et parse un fichier XML/JSON selon le type.
     */
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

    /**
     * Impossible de créer des feuilles de route avant le calcul des tournées.
     */
    @Override
    public List<Path> creerFeuillesDeRoute(Controlleur c) {
        throw IllegalStateException("Erreur : Impossible de créer de feuille de route si la tournée n'est pas encore calculé")
    }




    /**
     * Charge une tournée depuis un fichier et met à jour l'état si réussite.
     */
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

    /**
     * Pas de passage en mode modification possible dans cet état.
     */
    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        throw new IllegalStateException("Erreur : impossible de passer en mode modification avant le calcul de la tournée.");
    }

    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route avant le calcul de la tournée.");
    }

    /**
     * Impossible de sauvegarder une tournée avant son calcul.
     */
    @Override
    public Object sauvegarderTournee(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de sauvegarder une tournée avant son calcul.");
    }

    /**
     * Pas de sauvegarde de mofication possible
     */
    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        throw new IllegalStateException("Erreur : aucune modification à sauvegarder à ce stade.");
    }

    /**
     * Pas de modification possible
     */
    public Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse){
        throw new IllegalStateException("Erreur : Pas de modification de tournée possible dans l'état actuel");
    }

    /**
     * Retourne le nom de l'état courant.
     */
    @Override
    public String getNom() {
        return "Etat Demande de Livraison Chargee";
    }
}
