package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.modele.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;

import java.io.File;
import java.time.LocalTime;
import java.util.List;

public class EtatTourneeCalcule implements Etat {

    private Carte carte;
    private DemandeDeLivraison demande;
    private final List<Tournee> toutesLesTournees;

    public EtatTourneeCalcule(Carte carte, DemandeDeLivraison demande, List<Tournee> toutesLesTournees) {
        this.carte = carte;
        this.demande = demande;
        this.toutesLesTournees = toutesLesTournees;
    }

    @Override
    public Carte loadCarte(Controleur c, @RequestParam("file") MultipartFile file) {
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
    public Object loadDemandeLivraison(Controleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = uploadXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison) {
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte, (DemandeDeLivraison) dem));
            return dem;
        }
        return dem;
    }

    @Override
    public Object runCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        try {
            LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();

            CalculTournees t = new CalculTournees(carte, demande, vitesse, nombreLivreurs, heureDepart);
            List<Tournee> toutesLesTournees = t.calculerTournees();

            c.setCurrentState(new EtatTourneeCalcule(carte, demande, toutesLesTournees));
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
    public Object creerFeuillesDeRoute(Controleur c) {
        try {
            if (toutesLesTournees == null || toutesLesTournees.isEmpty()) {
                return "Aucune tournée à générer.";
            }

            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee t = toutesLesTournees.get(i);
                FeuilleDeRoute feuille = new FeuilleDeRoute(t);
                feuille.generateFeuilleDeRoute(i);

                System.out.println("Feuille de route créée pour la tournée #" + (i + 1));
            }

            return "Toutes les feuilles de route ont été générées avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    public Object saveTournee(Controleur c) {
        try {
            if (toutesLesTournees == null || toutesLesTournees.isEmpty()) {
                return "Aucune tournée à sauvegarder.";
            }

            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee t = toutesLesTournees.get(i);
                FeuilleDeRoute feuille = new FeuilleDeRoute(t);
                feuille.sauvegarderTournee();

                System.out.println("Tournée #" + (i + 1) + " sauvegardée avec succès !");
            }

            return "Toutes les tournées ont été sauvegardées avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    public Object loadTournee(Controleur c, MultipartFile file, Carte carte) {
        Object result = uploadXML("tournee", file, carte);

        if (result instanceof Tournee tournee) {
            List<Tournee> liste = List.of(tournee);
            c.setCurrentState(new EtatTourneeCalcule(carte, null, liste));
            return liste;
        }
        return result;
    }

    @Override
    public void passerEnModeSuppression(Controleur c, Tournee tournee) {
        if (tournee == null) {
            System.err.println("Erreur : aucune tournée fournie pour passer en mode suppression.");
            return;
        }
        c.setCurrentState(new EtatSuppressionLivraison(carte, tournee));
    }



    @Override
    public String getName() {
        return "Etat Tournée Calculé";
    }
}
