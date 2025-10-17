package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        try {
            //  Charger la carte
            File fichierCarte = new File("src/main/resources/donnees/plans/grandPlan.xml");
            Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
            System.out.println("Carte chargée.");

            //  Charger la demande de livraison
            File fichierDemande = new File("src/main/resources/donnees/demandes/demandeMoyen3.xml");
            DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
            System.out.println("Demande de livraison chargée.");

            //  Calculer la tournée
            double vitesse = 4.16; // m/s
            CalculTournee calculTournee = new CalculTournee(ville, demande, vitesse);
            System.out.println("Calcul de la tournée...");
            Tournee tournee = calculTournee.calculerTournee();
            System.out.println("Tournée calculée.\n");

            //  Affichage détaillé des chemins
            System.out.println("=== Tournée détaillée ===");
            double distanceTotale = 0;
            List<Chemin> chemins = tournee.getChemins();
            for (int i = 0; i < chemins.size(); i++) {
                Chemin c = chemins.get(i);
                NoeudDePassage depart = c.getNoeudDePassageDepart();
                NoeudDePassage arrivee = c.getNoeudDePassageArrivee();
                List<Troncon> troncons = c.getTroncons();
                double distanceChemin = c.getLongueurTotal();

                System.out.printf("Chemin %d :\n", i + 1);
                System.out.printf("  Départ : %s (id=%d, type=%s)\n", depart.getId(), depart.getId(), depart.getType());


                for (Troncon t : troncons) {
                    System.out.printf("    Troncon : %s, longueur = %.0f m\n", t.getnomRue(), t.longueur());
                }

                System.out.printf("  Arrivée : %s (id=%d, type=%s)\n", arrivee.getId(), arrivee.getId(), arrivee.getType());
                System.out.printf("  Distance totale chemin : %.0f m\n", distanceChemin);

            }

            System.out.println("=== Résumé de la tournée ===");
            System.out.printf("Distance totale : %.0f m\n", calculTournee.getLongueurTotale());
            System.out.printf("Durée totale : %.0f s\n", calculTournee.getDureeTotale());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
