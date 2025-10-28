package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.algorithme.CalculTournees;
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
    private int nombreLivreurs;

    public EtatTourneeCalcule(Carte carte, DemandeDeLivraison demande, int nombreLivreurs) {
        this.carte = carte;
        this.demande = demande;
        this.nombreLivreurs = nombreLivreurs;
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
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte, (DemandeDeLivraison) dem, nombreLivreurs));
            return dem;
        }
        return dem;
    }

    @Override
    public Object runCalculTournee(Controlleur c) {
        try {
            LocalTime heureDepart = this.demande.getEntrepot().getHoraireDepart() != null
                    ? this.demande.getEntrepot().getHoraireDepart()
                    : LocalTime.of(8, 0); // valeur par défaut

            CalculTournees t = new CalculTournees(
                    this.carte,
                    this.demande,
                    15.0, // vitesse m/s
                    heureDepart,
                    this.nombreLivreurs
            );

            List<Tournee> tournees = t.calculerTournees();
            return tournees;

        } catch (Exception e) {
            e.printStackTrace();
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
            if (tempFile != null && tempFile.exists())
                tempFile.delete();
        }
    }

    @Override
    public String getName() {
        return "Etat Tournée Calculée";
    }
}
