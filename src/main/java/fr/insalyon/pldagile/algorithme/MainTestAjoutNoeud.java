package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainTestAjoutNoeud {

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

            // Paramètres
            LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();
            double vitesse = 4.16; // m/s
            int nombreLivreurs = 1;

            // Calcul des tournées
            CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
            List<Tournee> toutesLesTournees = calculTournees.calculerTournees();

            // Suppression d’un noeud spécifique dans la première tournée
            Tournee tournee = toutesLesTournees.get(0);

            long idNoeudASupprimer = 1679901320; // exemple : noeud DELIVERY à supprimer
            ModificationTournee modifTournee = new ModificationTournee(new CalculChemins(ville), vitesse);
            tournee = modifTournee.supprimerNoeud(tournee, idNoeudASupprimer);
            tournee =  modifTournee.supprimerNoeud(tournee, 208769457);
            tournee =  modifTournee.supprimerNoeud(tournee, 208769120);
            tournee =  modifTournee.supprimerNoeud(tournee, 25336179);



            //AJOUT
            tournee = modifTournee.ajouterNoeud(tournee, 208769120L,2835339774L, 420, true);
            tournee = modifTournee.ajouterNoeud(tournee, 1679901320,208769120, 420, true);
            tournee = modifTournee.ajouterNoeud(tournee, 208769457,1679901320 , 600, false);
            tournee = modifTournee.ajouterNoeud(tournee, 25336179,208769457, 480, false);

            DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

            // Affichage des tournées et génération des feuilles de route
            Livreur livreur = tournee.getLivreur();

            System.out.printf("=== Tournée livreur %d ===\n", livreur.getId());

            // Génération de la feuille de route
            FeuilleDeRoute feuille = new FeuilleDeRoute(tournee);


            double distanceTotale = 0;
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
                        depart.getHoraireDepart().format(formatHeure)
                );

                double distanceChemin = 0;
                for (Troncon t : troncons) {
                    System.out.printf("    Tronçon : Rue %s, longueur = %.0f m\n",
                            t.getNomRue(), t.getLongueur());
                    distanceChemin += t.getLongueur();
                }

                System.out.printf("  Arrivée : %d (%s) à %s\n",
                        arrivee.getId(),
                        arrivee.getType(),
                        arrivee.getHoraireArrivee().format(formatHeure)
                );

                System.out.printf("  Distance : %.0f m\n\n", distanceChemin);
                distanceTotale += distanceChemin;
            }

            System.out.println("Résumé de la tournée :");
            System.out.printf("Distance totale : %.0f m\n", distanceTotale);
            System.out.printf("Durée totale : %.0f s\n", tournee.getDureeTotale());

            // Heure de fin correspond à l'arrivée à l'entrepôt
            NoeudDePassage entrepot = chemins.get(chemins.size() - 1).getNoeudDePassageArrivee();
            System.out.printf("Heure de fin estimée : %s\n\n", entrepot.getHoraireArrivee().format(formatHeure));
            //NoeudDePassage trouve = tournee.getNoeudParId(1679901320);
            // System.out.print(trouve);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
