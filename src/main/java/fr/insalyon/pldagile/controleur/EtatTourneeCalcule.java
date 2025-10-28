package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;

import java.io.File;

public class EtatTourneeCalcule implements Etat {

    private Carte carte;
    private DemandeDeLivraison demande;
    private final Tournee tournee;

    public EtatTourneeCalcule(Carte carte, DemandeDeLivraison demande, Tournee tournee) {
        this.carte = carte;
        this.demande = demande;
        this.tournee = tournee;
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
    public Object runCalculTournee(Controlleur c) {
        System.out.println("La tournée est déjà calculée.");
        return this.tournee;
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
    public Object creerFeuillesDeRoute(Controlleur c) {
        try {
            FeuilleDeRoute feuille = new FeuilleDeRoute(tournee);
            feuille.generateFeuilleDeRoute();

            System.out.println("Feuille de route créée avec succès !");
            return "Feuille de route générée avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    public Object saveTournee(Controlleur c) {
        try {
            FeuilleDeRoute feuille = new FeuilleDeRoute(tournee);
            feuille.sauvegarderTournee();

            System.out.println("Tournée sauvegardée avec succès !");
            return "Tournée sauvegardée avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    public Object loadTournee(Controlleur c, MultipartFile file, Carte carte) {
        Object result = uploadXML("tournee", file, carte);

        if (result instanceof Tournee tournee) {
            c.setCurrentState(new EtatTourneeCalcule(carte, null, tournee));
            return tournee;
        }
        return result;
    }


    @Override
    public String getName() {
        return "Etat Tournée Calculé";
    }
}
