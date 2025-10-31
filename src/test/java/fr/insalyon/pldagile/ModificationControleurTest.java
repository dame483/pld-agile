package fr.insalyon.pldagile;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.controleur.*;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste que la suppression, l’annulation et la restauration
 * agissent bien sur la même instance de tournée (pas une copie).
 */
public class ModificationControleurTest {

    private Controlleur controleur;
    private Tournee tournee;
    private Carte carte;

    @BeforeEach
    void setUp() throws Exception {
        // Initialise une carte et une tournée minimale

        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte carte = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(carte, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, carte);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();
        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(carte, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        tournee = toutesLesTournees.get(0);

        controleur = new Controlleur();

        EtatModificationTournee etat = new EtatModificationTournee(carte, tournee);
        controleur.setCurrentState(etat);
    }

    @Test
    void testSuppressionAnnulationRestaurationSurMemeTournee() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();

        Tournee tourneeEtat = etat.getTournee();

        // Vérifie qu'on a bien la même référence au départ
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");


        int cheminsAvant = tournee.getChemins().size();

        afficherTournee(tournee);



        // --- Suppression ---
        System.out.print("\n");
        System.out.println("---------------SUPPRESSION--------------");
        Map<String, Object> body = new HashMap<>();
        body.put("idNoeudPickup", 208769457);
        body.put("idNoeudDelivery", 1679901320);

        etat.modifierTournee(controleur, "supprimer", body, 4.1);

        int cheminsApresSuppression = tournee.getChemins().size();
        assertTrue(cheminsApresSuppression < cheminsAvant, "La tournée doit avoir perdu des chemins après suppression.");
        afficherTournee(tournee);


        // --- Annulation ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION--------------");
        controleur.annulerCommande();
        int cheminsApresAnnulation = tournee.getChemins().size();
        assertEquals(cheminsAvant, cheminsApresAnnulation, "Après annulation, la tournée doit retrouver son état initial.");
        afficherTournee(tournee);

        // --- Restauration ---
        System.out.print("\n");
        System.out.println("---------------RESTAURATION--------------");
        controleur.restaurerCommande();
        int cheminsApresRestauration = tournee.getChemins().size();
        assertTrue(cheminsApresRestauration < cheminsAvant, "Après restauration, la suppression doit être réappliquée.");

        // Vérifie que toutes les opérations ont agi sur la même instance
        assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
        afficherTournee(tournee);
    }


    private static final DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void afficherTournee(Tournee tournee) throws Exception {
        if (tournee == null) {
            System.out.println("La tournée est nulle !");
            return;
        }

        Livreur livreur = tournee.getLivreur();
        System.out.printf("=== Tournée livreur %d ===\n", livreur.getId());

        // Génération de la feuille de route (optionnel si tu veux juste afficher)
        FeuilleDeRoute feuille = new FeuilleDeRoute(tournee);
        feuille.generateFeuilleDeRoute(1);

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

        if (!chemins.isEmpty()) {
            NoeudDePassage entrepot = chemins.get(chemins.size() - 1).getNoeudDePassageArrivee();
            System.out.printf("Heure de fin estimée : %s\n\n", entrepot.getHoraireArrivee().format(formatHeure));
        }
    }



}
