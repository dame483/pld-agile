package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Représente l'état de l'application où une carte a été chargée.
 * Permet de charger des demandes de livraison ou des tournées,
 * mais empêche certaines opérations avant le chargement complet des données.
 */
@Component
public class EtatCarteChargee implements Etat {

    /** Carte actuellement chargée dans l'application. */
    private final Carte carte;

    /**
     * Constructeur de l'état avec une carte déjà chargée.
     *
     * @param c Carte chargée.
     */
    public EtatCarteChargee(Carte c) {
        this.carte = c;
    }

    /**
     * Charge une nouvelle carte et met à jour l'état.
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
     * Charge une demande de livraison et met à jour l'état si réussite.
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
     * Impossible de calculer une tournée sans demande de livraison.
     */
    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : impossible de calculer une tournée sans demande de livraison.");
    }

    /**
     * Impossible de créer des feuilles de route avant le calcul de la tournée.
     */
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
        throw new IllegalStateException("Erreur : impossible de passer en mode modification sans calcul de tournée.");
    }


    /**
     * Pas de sauvegarde de modification possible dans cet état.
     */
    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        throw new IllegalStateException("Erreur : aucune modification à sauvegarder à ce stade.");
    }

    public Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse){
        throw new IllegalStateException("Erreur : Pas de modification de tournée possible dans l'état actuel");
    }

    /**
     * Retourne le nom de l'état courant.
     */
    @Override
    public String getNom() {
        return "Etat Carte Chargee";
    }


}