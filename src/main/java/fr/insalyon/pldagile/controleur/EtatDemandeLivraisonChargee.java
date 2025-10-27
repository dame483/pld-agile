package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalTime;

@Component
public class EtatDemandeLivraisonChargee implements Etat {

    private final Carte carte;
    private final DemandeDeLivraison demLivraison;

    public EtatDemandeLivraisonChargee(Carte carte, DemandeDeLivraison demande) {
        this.carte = carte;
        this.demLivraison = demande;
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
    public Object runCalculTournee(Controlleur c) {
        try {
            LocalTime heureDepart = demLivraison.getEntrepot().getHoraireDepart();

            CalculTournee t = new CalculTournee(carte, demLivraison, 4.1, heureDepart);
            Tournee tournee = t.calculerTournee();

            c.setCurrentState(new EtatTourneeCalcule(carte, demLivraison));
            return tournee;

        } catch (Exception e) {
            return e;
        }
    }

    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) {
        if (file == null || file.isEmpty()) return null;

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            if ("carte".equalsIgnoreCase(type)) {
                return CarteParseurXML.loadFromFile(tempFile);
            } else {
                return DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public String getName() {
        return "Etat Demande de Livraison Chargee";
    }
}
