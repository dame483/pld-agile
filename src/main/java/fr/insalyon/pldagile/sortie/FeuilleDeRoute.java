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
            System.out.println("Feuille de route créée pour le livreur" );

        }
        else {
            System.out.println("Ce fichier existe déjà");
        }
            try (FileWriter writer = new FileWriter("src/main/java/fr/insalyon/pldagile/sortie/feuilleDeRoute.txt")) {
                writer.write("L'heure de départ de l'entrepôt : 08:00\n\n");

                List <Chemin> chemins = tournee.getChemins();
                for(int i = 0; i < chemins.size() ; i++) {
                    Chemin chemin = chemins.get(i);
                    writer.write("Chemin " + i + "\n");
                    NoeudDePassage depart = chemin.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();
                    writer.write("Tu pars de " + "(" + depart.getId() + "," + depart.getType() + ")\n");
                    for (int j = 0; j < chemin.getTroncons().size(); j++) {
                        Troncon troncon = chemin.getTroncons().get(j);
                        writer.write("Continuez " + troncon.longueur() + " m " + " sur " + troncon.getnomRue() + "\n");
                    }
                    writer.write("Tu arrives au point de livraison " +  "à " +  arrivee.getHoraireArrivee() + "\n");
                    writer.write("L'adresse de livraison " + "N° " + i + " est " + arrivee.getId()+ ")\n"); //pas le nom de rue je ne l'ai pas encore
                    writer.write("\n");
                }
            }
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    }

    public void sauvegarderTournee() throws Exception {
        List <Chemin> chemins = tournee.getChemins();
        JSONArray cheminsArray = new JSONArray();

        try {

            for (int i = 0; i < chemins.size(); i++) {
                JSONObject cheminObject = new JSONObject();
                Chemin chemin = chemins.get(i);
                int id = i + 1;
                String depart = "( " + chemin.getNoeudDePassageDepart().getId() + ","
                        + chemin.getNoeudDePassageDepart().getType() + " )";
                String arrivee = "( " + chemin.getNoeudDePassageArrivee().getId() + ","
                        + chemin.getNoeudDePassageArrivee().getType() + " )";
                String heureArrivee = chemin.getNoeudDePassageArrivee().getHoraireArrivee().toString();

                JSONArray rues = new JSONArray();

                for (int j = 0; j < chemin.getTroncons().size(); j++) {
                    Troncon troncon = chemin.getTroncons().get(j);
                    rues.put("Continuez " + troncon.longueur() + " m " + " sur " + troncon.getnomRue() + "\n");
                }

                cheminObject.put("id", id);
                cheminObject.put("depart", depart);
                cheminObject.put("arrivee", arrivee);
                cheminObject.put("heureArrivee", heureArrivee);
                cheminObject.put("rues" , rues);

                cheminsArray.put(cheminObject);
            }
            File jsonFile = new File("src/main/java/fr/insalyon/pldagile/sortie/sauvegardeTourne.json");
            try (FileWriter fileWriter =  new FileWriter(jsonFile)) {
                JSONObject tourneeObject = new JSONObject();
                tourneeObject.put("tournee", cheminsArray);
                fileWriter.write(tourneeObject.toString(4));
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la sauvegarde de la tournée", e);
        }
    }

}
