package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class EtatInitial implements Etat {

    public EtatInitial() {}

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

    @Override
    public Object chargerXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        if (file == null || file.isEmpty()) {
            throw new XMLFormatException("Fichier vide ou non sélectionné !");
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            return CarteParseurXML.loadFromFile(tempFile);

        } catch (XMLFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLFormatException("Erreur inattendue lors du chargement XML : " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("Impossible de supprimer le fichier temporaire");
                }
            }
        }
    }


    public Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse){
        throw new IllegalStateException("Erreur : Pas de modification de tournée possible dans l'état actuel");
    }

    @Override
    public Object chargerDemandeLivraison(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : Impossible de charger une demande de livraison avant de charger une carte.");
    }


    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        throw new IllegalStateException("Erreur : Impossible de créer une feuille de route avant le calcul de la tournée.");
    }

    @Override
    public Object sauvegarderTournee(Controleur c) {
        throw new IllegalStateException("Erreur : Impossible de sauvegarder une tournée qui n'a pas été calculée.");
    }


    @Override
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : Impossible de charger une tournée sans carte préalablement chargée.");
    }

    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        throw new IllegalStateException("Erreur : Aucune modification à sauvegarder à l’état initial.");
    }

    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : Impossible de calculer une tournée sans carte et demande de livraison.");
    }

    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        throw new IllegalStateException("Erreur : Impossible de passer en mode modification à l’état initial.");
    }

    @Override
    public String getNom() {
        return "Etat Initial";
    }

}