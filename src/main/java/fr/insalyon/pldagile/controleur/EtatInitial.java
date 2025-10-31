package fr.insalyon.pldagile.controleur;

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
    public Carte loadCarte(Controlleur c, MultipartFile file) {
        Carte carte = (Carte) uploadXML("carte", file, null);

        if (carte != null) {
            c.setCurrentState(new EtatCarteChargee(carte));
            return carte;
        } else {
            System.err.println("Erreur : carte non chargée");
            return null;
        }
    }

    @Override
    public Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte) {
        System.out.println("Impossible de charger une demande sans carte !");
        return null;
    }

    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) {
        if (file == null || file.isEmpty()) {
            System.err.println("Fichier vide !");
            return null;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            Carte result = CarteParseurXML.loadFromFile(tempFile);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("⚠Impossible de supprimer le fichier temporaire");
                }
            }
        }
    }

    @Override
    public Object creerFeuillesDeRoute(Controlleur c) {
        System.err.println("Erreur : impossible de créer une feuille de route avant le calcul de la tournée.");
        return null;
    }

    @Override
    public Object saveTournee(Controlleur c) {
        System.err.println("Erreur : impossible de sauvegarder une tournée avant son calcul.");
        return null;
    }

    @Override
    public Object loadTournee(Controlleur c, MultipartFile file, Carte carte) {
        System.err.println("Erreur : impossible de charger une tournée avant de charger une carte associée.");
        return null;
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