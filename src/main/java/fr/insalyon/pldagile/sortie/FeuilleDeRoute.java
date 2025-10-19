package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FeuilleDeRoute {
    private Tournee tournee;
    public FeuilleDeRoute(Tournee tournee) {
        this.tournee = tournee;
    }
    public void GenerateFeuilleDeRoute(Tournee tournee) throws Exception {
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
                    writer.write("Chemin " + i+1 + "\n");
                    NoeudDePassage depart = chemin.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();
                    writer.write("Tu parts de " + "(" + depart.getId() + "," + depart.getType() + ")\n");
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

}
