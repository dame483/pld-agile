package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.exception.XMLFormatException;
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
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        Object dem = uploadXML("demande", file, this.carte);
        if (dem instanceof DemandeDeLivraison demande) {
            c.setCurrentState(new EtatDemandeLivraisonChargee(this.carte, demande));
            return demande;
        }
        return dem;
    }

    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        if (file == null || file.isEmpty()) {
            throw new XMLFormatException("Le fichier est vide ou nul.");
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
                    throw new XMLFormatException("Type de fichier non reconnu : " + type);
            }

            return result;

        } catch (XMLFormatException e) {
            throw e;

        } catch (Exception e) {
            throw new XMLFormatException("Erreur lors du chargement du fichier XML/JSON : " + e.getMessage(), e);

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

        if (result instanceof Exception e) {
            return e;
        }

        List<Tournee> toutesLesTournees;

        if (result instanceof Tournee tournee) {
            toutesLesTournees = List.of(tournee);
        } else if (result instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
            toutesLesTournees = (List<Tournee>) liste;
        } else {
            return new Exception("Fichier JSON invalide ou format incorrect");
        }

        c.setCurrentState(new EtatTourneeCalcule(carte, null, toutesLesTournees));

        return toutesLesTournees;
    }


    @Override
    public String getName() {
        return "Etat Carte Chargee";
    }


}