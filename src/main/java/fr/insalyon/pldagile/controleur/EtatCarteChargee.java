package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

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

            switch (type.toLowerCase()) {
                case "carte":
                    result = CarteParseurXML.loadFromFile(tempFile);
                    break;

                case "demande":
                    result = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                    break;

                case "tournee":
                    result = parseurTourneeJson.parseurTournee(tempFile.getAbsolutePath());
                    break;

                default:
                    throw new IllegalArgumentException("Type de fichier non reconnu : " + type);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du fichier XML/JSON : " + e.getMessage());
            e.printStackTrace();
            return e;
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public Object runCalculTournee(Controlleur c, int nombreLivreurs) {
        System.err.println("Erreur : impossible de calculer une tournée sans demande de livraison.");
        return null;
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
        Object result = uploadXML("tournee", file, carte);

        if (result instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
            List<Tournee> toutesLesTournees = (List<Tournee>) liste;
            c.setCurrentState(new EtatTourneeCalcule(carte, null, toutesLesTournees));
            return toutesLesTournees;
        }
        return result;
    }


    @Override
    public String getName() {
        return "Etat Carte Chargee";
    }


}