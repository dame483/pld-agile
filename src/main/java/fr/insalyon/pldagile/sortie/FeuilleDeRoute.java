package fr.insalyon.pldagile.sortie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeuilleDeRoute {
    private Tournee tournee;

    public FeuilleDeRoute(Tournee tournee) {
        this.tournee = tournee;
    }

    public void generateFeuilleDeRoute() throws Exception {
        File feuilleDeRoute = new File("src/main/java/fr/insalyon/pldagile/sortie/feuilleDeRoute.txt");
        try {
            if (feuilleDeRoute.createNewFile()) {
                System.out.println("Feuille de route créée pour le livreur");

            } else {
                System.out.println("Ce fichier existe déjà");
            }
            try (FileWriter writer = new FileWriter("src/main/java/fr/insalyon/pldagile/sortie/feuilleDeRoute.txt")) {
                writer.write("L'heure de départ de l'entrepôt : 08:00\n\n");

                List<Chemin> chemins = tournee.getChemins();
                for (int i = 0; i < chemins.size(); i++) {
                    Chemin chemin = chemins.get(i);
                    writer.write("Chemin " + i + "\n");
                    NoeudDePassage depart = chemin.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();
                    writer.write("Tu pars de " + "(" + depart.getId() + "," + depart.getType() + ")\n");
                    for (int j = 0; j < chemin.getTroncons().size(); j++) {
                        Troncon troncon = chemin.getTroncons().get(j);
                        writer.write("Continuez " + troncon.getLongueur() + " m " + " sur " + troncon.getNomRue() + "\n");
                    }
                    writer.write("Tu arrives au point de livraison " + "à " + arrivee.getHoraireArrivee() + "\n");
                    writer.write("L'adresse de livraison " + "N° " + i + " est " + arrivee.getId() + ")\n"); //pas le nom de rue je ne l'ai pas encore
                    writer.write("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sauvegarderTournee() throws Exception {
        List<Chemin> chemins = tournee.getChemins();
        JSONArray cheminsArray = new JSONArray();

        try {
            for (Chemin chemin : chemins) {
                JSONObject cheminObject = new JSONObject();

                JSONArray tronconsArray = new JSONArray();
                for (Troncon troncon : chemin.getTroncons()) {
                    JSONObject tronconObject = new JSONObject();
                    tronconObject.put("idOrigine", troncon.getIdOrigine());
                    tronconObject.put("idDestination", troncon.getIdDestination());
                    tronconObject.put("longueur", troncon.getLongueur());
                    tronconObject.put("nomRue", troncon.getNomRue());
                    tronconsArray.put(tronconObject);
                }

                cheminObject.put("troncons", tronconsArray);
                cheminObject.put("longueurTotale", chemin.getLongueurTotal());

                JSONObject noeuddePassageDepart = new JSONObject();
                noeuddePassageDepart.put("id", chemin.getNoeudDePassageDepart().getId());
                noeuddePassageDepart.put("latitude", chemin.getNoeudDePassageDepart().getLatitude());
                noeuddePassageDepart.put("longitude", chemin.getNoeudDePassageDepart().getLongitude());
                noeuddePassageDepart.put("typeNoeud", chemin.getNoeudDePassageDepart().getType());
                noeuddePassageDepart.put("horaireArrivee", chemin.getNoeudDePassageDepart().getHoraireArrivee().toString());
                cheminObject.put("NoeudDePassageDepart", noeuddePassageDepart);

                JSONObject noeudDePassageArrivee = new JSONObject();
                noeudDePassageArrivee.put("id", chemin.getNoeudDePassageArrivee().getId());
                noeudDePassageArrivee.put("latitude", chemin.getNoeudDePassageArrivee().getLatitude());
                noeudDePassageArrivee.put("longitude", chemin.getNoeudDePassageArrivee().getLongitude());
                noeudDePassageArrivee.put("typeNoeud", chemin.getNoeudDePassageArrivee().getType());
                noeudDePassageArrivee.put("horaireArrivee", chemin.getNoeudDePassageArrivee().getHoraireArrivee().toString());
                cheminObject.put("NoeudDePassageArrivee", noeudDePassageArrivee);

                cheminsArray.put(cheminObject);
            }
                File jsonFile = new File("src/main/java/fr/insalyon/pldagile/sortie/sauvegardeTourne.json");
                try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                    JSONObject tourneeObject = new JSONObject();
                    tourneeObject.put("tournee", cheminsArray);
                    fileWriter.write(tourneeObject.toString(4));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("Erreur lors de la sauvegarde de la tournée", e);
                }
            }
        catch (Exception e) {
            e.printStackTrace();
        }
        }
    }






