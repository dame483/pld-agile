package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
public class EtatCarteChargee implements Etat {

    private final Carte carte;

    public EtatCarteChargee(Carte c) {
        this.carte = c;
    }

    @Override
    public Carte loadCarte(Controlleur c, @RequestParam("file") MultipartFile file) {
        Carte nouvelleCarte = (Carte) uploadXML("carte", file, this.carte);

        if (nouvelleCarte == null) {
            c.setCurrentState(new EtatInitial());
            return null;
        }

        c.setCurrentState(new EtatCarteChargee(nouvelleCarte));
        return nouvelleCarte;
    }

    @Override
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = uploadXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison demande) {
            c.setCurrentState(new EtatDemandeLivraisonChargee(this.carte, demande));
            return demande;
        }
        return dem;
    }

    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) {
        if (file == null || file.isEmpty()) {
            System.err.println("Le fichier est vide ou nul.");
            return null;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            System.out.println("Fichier temporaire créé : " + tempFile.getAbsolutePath());
            Object result;

            if ("carte".equalsIgnoreCase(type)) {
                result = CarteParseurXML.loadFromFile(tempFile);
            } else {
                result = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement XML : " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public Object runCalculTournee(Controlleur c) {
        System.err.println("Erreur : impossible de calculer une tournée sans demande de livraison.");
        return null;
    }

    @Override
    public Object saveTournee(Controlleur c) {
        System.err.println("Erreur : impossible de sauvegarder une tournée sans tournée calculé.");
        return null;
    }

    @Override
    public String getName() {
        return "Etat Carte Chargee";
    }


}
