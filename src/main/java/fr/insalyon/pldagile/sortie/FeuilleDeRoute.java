package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.io.FileWriter;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeuilleDeRoute {
    private Tournee tournee;

    public FeuilleDeRoute(Tournee tournee) {
        this.tournee = tournee;
    }

    public void genererFeuilleDeRoute(int index) throws Exception {
        File feuilleDeRoute = new File("src/main/java/fr/insalyon/pldagile/sortie/feuilleDeRoute/feuilleDeRoute" + (index + 1) + ".txt");
        try {
            if (feuilleDeRoute.createNewFile()) {
                System.out.println("Feuille de route créée pour le livreur");

            } else {
                System.out.println("Ce fichier existe déjà");
            }
            try (FileWriter writer = new FileWriter("src/main/java/fr/insalyon/pldagile/sortie/feuilleDeRoute/feuilleDeRoute" + (index + 1) + ".txt")) {

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }






