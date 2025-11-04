package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.Tournee;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
public class EtatInitial implements Etat {

    public EtatInitial() {}

    @Override
    public Carte loadCarte(Controlleur c, MultipartFile file) throws XMLFormatException {
        Object result = uploadXML("carte", file, null);

        if (result instanceof Carte carte) {
            c.setCurrentState(new EtatCarteChargee(carte));
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
    public Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte) {
        System.out.println("Impossible de charger une demande sans carte !");
        return null;
    }

    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
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
                    System.err.println("⚠ Impossible de supprimer le fichier temporaire");
                }
            }
        }
    }


    @Override
    public Object creerFeuillesDeRoute(Controlleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route avant le calcul de la tournée.");
    }

    @Override
    public Object saveTournee(Controlleur c) {
        throw new IllegalStateException("Impossible de sauvegarder une tournée qui n'a pas été calculé.");
    }


    @Override
    public Object loadTournee(Controlleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Impossible de charger une tournée sans carte préalablement chargée.");
    }


    @Override
    public String getName() {
        return "Etat Initial";
    }

    @Override
    public Object runCalculTournee(Controlleur c, int nombreLivreurs, double vitesse) {
        return null;
    }

    @Override
    public void passerEnModeModification(Controlleur c, Tournee tournee){return;}

}