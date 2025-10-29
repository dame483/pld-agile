package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.modele.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalTime;
import java.util.List;

@Component
public class EtatTourneeCalcule implements Etat {

    private Carte carte;
    private DemandeDeLivraison demande;

    public EtatTourneeCalcule(Carte carte, DemandeDeLivraison demande) {
        this.carte = carte;
        this.demande = demande;
    }

    @Override
    public Carte loadCarte(Controlleur c, @RequestParam("file") MultipartFile file) {
        Carte newCarte = (Carte) uploadXML("carte", file, this.carte);
        if (newCarte != null) {
            c.setCurrentState(new EtatCarteChargee(newCarte));
            return newCarte;
        } else {
            System.err.println("Erreur : carte non chargée");
            return null;
        }
    }

    @Override
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = uploadXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison) {
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte, (DemandeDeLivraison) dem));
            return dem;
        }
        return dem;
    }

    @Override
    public Object runCalculTournee(Controlleur c, int nombreLivreurs) {
        try {
            LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();

            CalculTournees t = new CalculTournees(carte, demande, 4.1, nombreLivreurs, heureDepart);
            List<Tournee> toutesLesTournees = t.calculerTournees();

            c.setCurrentState(new EtatTourneeCalcule(carte, demande));
            return toutesLesTournees;

        } catch (Exception e) {
            return e;
        }
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

            if ("carte".equalsIgnoreCase(type)) {
                return CarteParseurXML.loadFromFile(tempFile);
            } else {
                return DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        } finally {
            if (tempFile != null && tempFile.exists()) tempFile.delete();
        }
    }

    @Override
    public String getName() {
        return "Etat Tournée Calculé";
    }
}