package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.io.FileWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe représentant une feuille de route générée pour une tournée de livraison.
 * Elle permet de créer un fichier texte détaillant les chemins à suivre pour le livreur.
 */
public class FeuilleDeRoute {

    /** La tournée associée à cette feuille de route. */
    private Tournee tournee;

    /**
     * Constructeur d'une feuille de route pour une tournée donnée.
     * @param tournee la tournée à utiliser pour générer la feuille de route
     */
    public FeuilleDeRoute(Tournee tournee) {
        this.tournee = tournee;
    }

    /**
     * Génère la feuille de route sous forme de fichier texte temporaire.
     * Le fichier contient les instructions de parcours pour chaque chemin de la tournée.
     *
     * @param index numéro de la feuille de route (utile pour différencier les fichiers temporaires)
     * @return le chemin vers le fichier texte généré
     * @throws Exception en cas de problème d'écriture de fichier
     */
    public Path genererFeuilleDeRoute(int index) throws Exception {
        Path feuilleDeRoute = Files.createTempFile("feuilleDeRoute" + (index + 1) + "-", ".txt");
        try {
            try (FileWriter writer = new FileWriter(feuilleDeRoute.toFile())) {

                writer.write("L'heure de départ de l'entrepôt : " + tournee.getChemins().get(0).getNoeudDePassageDepart().getHoraireDepart()+ "\n\n");

                List<Chemin> chemins = tournee.getChemins();
                for (int i = 0; i < chemins.size(); i++) {
                    Chemin chemin = chemins.get(i);
                    writer.write("Chemin " + (i+1) + "\n");
                    NoeudDePassage depart = chemin.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();
                    writer.write("Tu pars de " + "(" + depart.getId() + "," + depart.getType() + ")\n");
                    for (int j = 0; j < chemin.getTroncons().size(); j++) {
                        Troncon troncon = chemin.getTroncons().get(j);
                        writer.write("Continuez environ " +(int) Math.ceil(troncon.getLongueur()) + " m " + " sur " + troncon.getNomRue() + "\n");
                    }
                    writer.write("Tu arrives au point de livraison " + "à " + arrivee.getHoraireArrivee() + "\n");
                    writer.write("L'id de l'adresse de livraison " + "N° " + (i + 1) + " est " + "(" + arrivee.getId() + "," + arrivee.getType() + ")\n");
                    writer.write("\n");
                }
                return  feuilleDeRoute;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feuilleDeRoute;
    }
    }






