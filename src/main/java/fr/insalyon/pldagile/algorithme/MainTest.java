package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

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
            System.out.println("Nombre de livraisons : " + demande.getLivraisons().size());

            //  Paramètres
            double vitesse = 4.16; // m/s (~15 km/h)
            LocalTime heureDepart = demande.getEntrepot().getHoraireArrivee() != null
                    ? demande.getEntrepot().getHoraireArrivee()
                    : LocalTime.of(8, 0); // défaut 08:00

            int nombreLivreurs = 2;

            //  Clustering K-Means des livraisons
            KMeans kmeans = new KMeans(demande.getLivraisons(), nombreLivreurs);
            List<List<Livraison>> clusters = kmeans.cluster();

            //  Calcul et affichage des tournées par livreur
            DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (int i = 0; i < clusters.size(); i++) {
                List<Livraison> clusterLivraisons = clusters.get(i);

                // Créer une demande spécifique pour ce cluster
                DemandeDeLivraison demandeCluster = new DemandeDeLivraison(demande.getEntrepot(), clusterLivraisons);
                CalculTournee calculTournee = new CalculTournee(ville, demandeCluster, vitesse, heureDepart);
                Tournee tournee = calculTournee.calculerTournee();

                System.out.println("\n=== Tournée livreur " + (i + 1) + " ===");
                List<Chemin> chemins = tournee.getChemins();

                for (int j = 0; j < chemins.size(); j++) {
                    Chemin c = chemins.get(j);
                    NoeudDePassage depart = c.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = c.getNoeudDePassageArrivee();
                    List<Troncon> troncons = c.getTroncons();

                    System.out.printf("Chemin %d :\n", j + 1);
                    System.out.printf("  Départ : %d (%s) à %s\n",
                            depart.getId(),
                            depart.getType(),
                            depart.getHoraireDepart().format(formatHeure));

                    double distanceChemin = 0;
                    for (Troncon t : troncons) {
                        System.out.printf("    Tronçon : Rue %s, longueur = %.0f m\n",
                                t.getnomRue(), t.longueur());
                        distanceChemin += t.longueur();
                    }

                    System.out.printf("  Arrivée : %d (%s) à %s\n",
                            arrivee.getId(),
                            arrivee.getType(),
                            arrivee.getHoraireArrivee().format(formatHeure));
                    System.out.printf("  Distance : %.0f m\n\n", distanceChemin);
                }

                // Utiliser CalculTournee pour récupérer longueur et durée totales
                System.out.printf("Distance totale : %.0f m\n", calculTournee.getLongueurTotale());
                System.out.printf("Durée totale : %.0f s\n", calculTournee.getDureeTotale());

                LocalTime heureFin = heureDepart.plusSeconds(Math.round(calculTournee.getDureeTotale()));
                System.out.printf("Heure de fin estimée : %s\n", heureFin.format(formatHeure));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
