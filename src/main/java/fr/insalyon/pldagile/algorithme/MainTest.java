package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        try {
            // Charger la carte
            File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
            Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
            System.out.println("Carte chargée.");

            // Charger la demande de livraison
            File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
            DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
            System.out.println("Demande de livraison chargée.");
            System.out.println("Nombre de livraisons : " + demande.getLivraisons().size());

            // Paramètres
            double vitesse = 4.16; // m/s (~15 km/h)
            LocalTime heureDepart = demande.getEntrepot().getHoraireArrivee() != null
                    ? demande.getEntrepot().getHoraireArrivee()
                    : LocalTime.of(8, 0); // défaut 08:00

            int nombreLivreurs = 2; // Exemple : 2 livreurs

            // Calculateur de tournées
            CalculateurTournees calculateur = new CalculateurTournees(ville, demande, vitesse, heureDepart, nombreLivreurs);
            List<Tournee> toutesLesTournees = calculateur.calculerTournees();

            // Affichage
            DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee tournee = toutesLesTournees.get(i);
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

                System.out.printf("Distance totale : %.0f m\n", tournee.getDureeTotale()); // si tu veux ajouter longueurTotale, tu peux créer getter
                System.out.printf("Durée totale : %.0f s\n", tournee.getDureeTotale());

                LocalTime heureFin = heureDepart.plusSeconds(Math.round(tournee.getDureeTotale()));
                System.out.printf("Heure de fin estimée : %s\n", heureFin.format(formatHeure));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
