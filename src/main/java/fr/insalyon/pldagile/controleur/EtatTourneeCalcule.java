package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.SauvegarderTournee;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
import org.springframework.stereotype.Component;
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
        if (dem instanceof DemandeDeLivraison) {
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte, (DemandeDeLivraison) dem));
            return dem;
        }
        return dem;
    }

    @Override
    public Object runCalculTournee(Controlleur c, int nombreLivreurs, double vitesse) {
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
    public Object creerFeuillesDeRoute(Controlleur c) {
        try {
            if (toutesLesTournees == null || toutesLesTournees.isEmpty()) {
                return "Aucune tournée à générer.";
            }

            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee t = toutesLesTournees.get(i);
                FeuilleDeRoute feuille = new FeuilleDeRoute(t);
                feuille.genererFeuilleDeRoute(i);

                System.out.println("Feuille de route créée pour la tournée #" + (i + 1));
            }

            return "Toutes les feuilles de route ont été générées avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    public Object saveTournee(Controlleur c) {
        try {
            if (toutesLesTournees == null || toutesLesTournees.isEmpty()) {
                return "Aucune tournée à sauvegarder.";
            }
            SauvegarderTournee sauvegarderTournee = new SauvegarderTournee(toutesLesTournees, c.getDemande());
            sauvegarderTournee.sauvegarderTournee();
            System.out.println("Tournées sauvegardées avec succès !");

            return "Toutes les tournées ont été sauvegardées avec succès.";

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
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
    public void passerEnModeModification(Controlleur c, Tournee tournee) {
        if (tournee == null) {
            System.err.println("Erreur : aucune tournée fournie pour passer en mode modification.");
            return;
        }
        c.setCurrentState(new EtatModificationTournee(carte, tournee));
    }



    @Override
    public String getName() {
        return "Etat Tournée Calculé";
    }
}
