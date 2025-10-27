package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        try {
            //  Charger la carte
            File fichierCarte = new File("src/main/resources/donnees/plans/moyenPlan.xml");
            Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
            System.out.println("Carte chargée.");

            //  Charger la demande de livraison
            File fichierDemande = new File("src/main/resources/donnees/demandes/demandeMoyen5.xml");
            DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
            System.out.println("Demande de livraison chargée.");

            //  Paramètres de la tournée : heure de départ depuis le parseur ou défaut 08:00
            LocalTime heureDepart;
            if (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null) {
                heureDepart = demande.getEntrepot().getHoraireDepart();
            } else {
                heureDepart = LocalTime.of(8, 0);
            }

            double vitesse = 4.16; // m/s

            //  Calcul de la tournée
            CalculTournee calculTournee = new CalculTournee(ville, demande, vitesse, heureDepart);
            System.out.println("\nCalcul de la tournée...");
            Tournee tournee = calculTournee.calculerTournee();
            System.out.println("Tournée calculée.\n");
            //Génération de feuille de route
            FeuilleDeRoute feuilleDeRoute = new FeuilleDeRoute(tournee);
            feuilleDeRoute.generateFeuilleDeRoute();
            //Sauvegarde tournée
            feuilleDeRoute.sauvegarderTournee();
            //  Affichage détaillé des chemins
            System.out.println("=== Tournée détaillée ===");
            DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

            double distanceTotale = 0;
            List<Chemin> chemins = tournee.getChemins();
            for (int i = 0; i < chemins.size(); i++) {
                Chemin c = chemins.get(i);
                NoeudDePassage depart = c.getNoeudDePassageDepart();
                NoeudDePassage arrivee = c.getNoeudDePassageArrivee();
                List<Troncon> troncons = c.getTroncons();

                System.out.printf("Chemin %d :\n", i + 1);
                System.out.printf("  Départ : %d (%s) à %s\n",
                        depart.getId(),
                        depart.getType(),
                        depart.getHoraireDepart().format(formatHeure)
                );

                double distanceChemin = 0;
                for (Troncon t : troncons) {
                    System.out.printf("    Tronçon : Rue %s, longueur = %.0f m\n",
                            t.getnomRue(), t.longueur());
                    distanceChemin += t.longueur();
                }

                System.out.printf("  Arrivée : %d (%s) à %s\n",
                        arrivee.getId(),
                        arrivee.getType(),
                        arrivee.getHoraireArrivee().format(formatHeure)
                );

                System.out.printf("  Distance : %.0f m\n\n", distanceChemin);
                distanceTotale += distanceChemin;
            }

            System.out.println("=== Résumé de la tournée ===");
            System.out.printf("Distance totale : %.0f m\n", distanceTotale);
            System.out.printf("Durée totale : %.0f s\n", tournee.getDureeTotale());

            // Heure de fin correspond à l'arrivée à l'entrepôt
            LocalTime heureFin = demande.getEntrepot().getHoraireArrivee();
            System.out.printf("Heure de fin estimée : %s\n", heureFin.format(formatHeure));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
