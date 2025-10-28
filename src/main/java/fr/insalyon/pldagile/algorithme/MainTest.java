package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.exception.TourneeNonConnexeException;

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
            System.out.println("Carte chargée");

            //  Charger la demande de livraison
            File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
            DemandeDeLivraison demande =
                    DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
            System.out.println("Demande de livraison chargée ");
            System.out.println("Nombre de livraisons : " + demande.getLivraisons().size());

            // Paramètres
            double vitesse = 4.16; // 15km/h en m/s
            LocalTime heureDepart = demande.getEntrepot().getHoraireArrivee() != null
                    ? demande.getEntrepot().getHoraireArrivee()
                    : LocalTime.of(8, 0);

            int nombreLivreurs = 2;

            // Calculateur de tournées multi-livreurs
            CalculTournees calculateur =
                    new CalculTournees(ville, demande, vitesse, heureDepart, nombreLivreurs);
            List<Tournee> toutesLesTournees = calculateur.calculerTournees();

            // Affichage formaté
            DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (int i = 0; i < toutesLesTournees.size(); i++) {
                Tournee tournee = toutesLesTournees.get(i);

                System.out.println("\n===============================");
                System.out.println(" Tournée du livreur " + (i + 1));
                System.out.println("===============================");

                List<Chemin> chemins = tournee.getChemins();
                for (int j = 0; j < chemins.size(); j++) {
                    Chemin c = chemins.get(j);
                    NoeudDePassage depart = c.getNoeudDePassageDepart();
                    NoeudDePassage arrivee = c.getNoeudDePassageArrivee();

                    System.out.printf("  Chemin %d : %d → %d\n",
                            j + 1,
                            depart.getId(),
                            arrivee.getId());

                    System.out.printf("  Départ à : %s\n",
                            depart.getHoraireDepart().format(formatHeure));

                    double distanceChemin = c.getLongueurTotal();
                    System.out.printf("  Distance : %.0f m\n", distanceChemin);

                    System.out.printf("  Arrivée à : %s\n\n",
                            arrivee.getHoraireArrivee().format(formatHeure));
                }

                System.out.printf("Distance totale : %.0f m\n", tournee.getLongueuerTotale());
                System.out.printf(" Durée totale : %.0f s\n", tournee.getDureeTotale());
                LocalTime heureFin = heureDepart.plusSeconds(Math.round(tournee.getDureeTotale()));
                System.out.printf(" Heure de fin : %s\n", heureFin.format(formatHeure));
            }

            System.out.println("\n Simulation terminée avec succès !");

        } catch (TourneeNonConnexeException e) {
            System.err.println(" Erreur : le graphe n’est pas connexe !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
