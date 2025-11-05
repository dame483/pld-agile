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
        double vitesse = 4.1;
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
    void testAjoutAnnulationRestaurationSurMemeTournee() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();
        Tournee tourneeEtat = etat.getTournee();

        // Vérifie que l’état référence bien la même tournée
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        // Sauvegarde des chemins initiaux
        List<Chemin> cheminsAvant = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        int cheminsAvantLongueur = tournee.getChemins().size();

        afficherTournee(tournee);

        // --- Ajout ---
        System.out.println("---------------AJOUT--------------");
        Map<String, Object> body = new HashMap<>();
        body.put("idNoeudPickup", 459797860L);   // Id fictif à ajouter
        body.put("idNoeudDelivery", 55475018L);
        body.put("idPrecedentPickup", 208769457L);   // Id nœud avant lequel on insère
        body.put("idPrecedentDelivery", 1679901320L);
        body.put("dureeEnlevement", 300.0);  // durée fictive en secondes
        body.put("dureeLivraison", 600.0);

        etat.modifierTournee(controleur, "ajouter", body, 4.1);

        List<Chemin> cheminsApresAjout = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        int cheminsApresAjoutLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresAjoutLongueur > cheminsAvantLongueur, "La tournée doit avoir gagné des chemins après ajout.");
        afficherTournee(tournee);

        // --- Annulation 1 ---
        System.out.println("---------------ANNULATION --------------");
        controleur.annulerCommande();
        List<Chemin> cheminsApresAnnulation1 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        assertEquals(cheminsAvant, cheminsApresAnnulation1, "Après annulation, la tournée doit revenir à son état initial.");
        afficherTournee(tournee);


        // --- Restauration 1 ---
        System.out.println("---------------RESTAURATION --------------");
        controleur.restaurerCommande();
        List<Chemin> cheminsApresRestauration1 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        assertEquals(cheminsApresAjout, cheminsApresRestauration1, "Après restauration, l'ajout doit être réappliqué.");
        afficherTournee(tournee);

        // --- Annulation 1 ---
        System.out.println("---------------ANNULATION 2--------------");
        controleur.annulerCommande();
        List<Chemin> cheminsApresAnnulation2 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        assertEquals(cheminsAvant, cheminsApresAnnulation2, "Après annulation, la tournée doit revenir à son état initial.");
        afficherTournee(tournee);

        // --- Restauration 1 ---
        System.out.println("---------------RESTAURATION 2--------------");
        controleur.restaurerCommande();
        List<Chemin> cheminsApresRestauration2 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());
        assertEquals(cheminsApresAjout, cheminsApresRestauration2, "Après restauration, l'ajout doit être réappliqué.");
        afficherTournee(tournee);


        System.out.println("---------------AJOUT NOEUD 2--------------");
        Map<String, Object> body2 = new HashMap<>();
        body2.put("idNoeudPickup", 208769039L);   // Id fictif à ajouter
        body2.put("idNoeudDelivery", 25173820L);
        body2.put("idPrecedentPickup", 208769457L);   // Id nœud avant lequel on insère
        body2.put("idPrecedentDelivery", 55475018L);
        body2.put("dureeEnlevement", 300.0);  // durée fictive en secondes
        body2.put("dureeLivraison", 600.0);


        etat.modifierTournee(controleur, "ajouter", body2, 4.1);


        afficherTournee(tournee);

        // --- ANNULATION 1 ---
        System.out.println("---------------ANNULATION 1--------------");
        controleur.annulerCommande();
        afficherTournee(tournee);

        // --- ANNULATION 2 ---
        System.out.println("---------------ANNULATION 2--------------");
        controleur.annulerCommande();  // Reviens à l’état avant l’ajout initial
        afficherTournee(tournee);

        // --- RESTAURATION 1 ---
        System.out.println("---------------RESTAURATION 1--------------");
        controleur.restaurerCommande();
        afficherTournee(tournee);

        // --- RESTAURATION 2 ---
        System.out.println("---------------RESTAURATION 2--------------");
        controleur.restaurerCommande();
        afficherTournee(tournee);


    }


    @Test
    void testSuppressionAnnulationRestaurationSurMemeTournee() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();

        Tournee tourneeEtat = etat.getTournee();

        // Vérifie qu'on a bien la même référence au départ
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        List<Chemin> cheminsAvant = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsAvantLongueur = tournee.getChemins().size();

        afficherTournee(tournee);

        // --- Suppression ---
        System.out.print("\n");
        System.out.println("---------------SUPPRESSION--------------");
        Map<String, Object> body = new HashMap<>();
        body.put("idNoeudPickup", 208769457);
        body.put("idNoeudDelivery", 1679901320);

        etat.modifierTournee(controleur, "supprimer", body, 4.1);

        List<Chemin> cheminsApresSuppression = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresSuppressionLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresSuppressionLongueur < cheminsAvantLongueur, "La tournée doit avoir perdu des chemins après suppression.");
        afficherTournee(tournee);

        // --- Annulation ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION--------------");
        controleur.annulerCommande();

        List<Chemin> cheminsApresAnnulation = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresAnnulationLongueur = tournee.getChemins().size();
        assertEquals(cheminsAvantLongueur, cheminsApresAnnulationLongueur, "Après annulation, la tournée doit retrouver son état initial.");
        assertEquals(cheminsApresAnnulation, cheminsAvant);

        afficherTournee(tournee);

        // --- Restauration ---
        System.out.print("\n");
        System.out.println("---------------RESTAURATION--------------");
        controleur.restaurerCommande();

        List<Chemin> cheminsApresRestauration = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresRestaurationLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresRestaurationLongueur < cheminsAvantLongueur, "Après restauration, la suppression doit être réappliquée.");
        assertEquals(cheminsApresSuppression, cheminsApresRestauration);

        afficherTournee(tournee);

        // --- Annulation 2 ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION 2--------------");
        controleur.annulerCommande();

        List<Chemin> cheminsApresDeuxiemeAnnulation = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresDeuxiemeAnnulationLongueur = tournee.getChemins().size();
        assertEquals(cheminsAvantLongueur, cheminsApresDeuxiemeAnnulationLongueur, "Après la 2e annulation, la tournée doit revenir à l’état initial.");
        assertEquals(cheminsApresDeuxiemeAnnulation, cheminsAvant);

        afficherTournee(tournee);

        // --- Restauration 2 ---
        System.out.print("\n");
        System.out.println("---------------RESTAURATION 2--------------");
        controleur.restaurerCommande();

        List<Chemin> cheminsApresDeuxiemeRestauration = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresDeuxiemeRestaurationLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresDeuxiemeRestaurationLongueur < cheminsAvantLongueur, "Après la 2e restauration, la suppression doit être réappliquée.");
        assertEquals(cheminsApresSuppression, cheminsApresDeuxiemeRestauration);

        // Vérifie que toutes les opérations ont agi sur la même instance
        assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
        afficherTournee(tournee);

        System.out.println("---------------SUPPRESSION NOEUD 2--------------");
        Map<String, Object> body2= new HashMap<>();
        body2.put("idNoeudPickup", 208769120L);
        body2.put("idNoeudDelivery", 25336179L);

        etat.modifierTournee(controleur, "supprimer", body2, 4.1);

        List<Chemin> cheminsApresSuppression2 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresSuppressionLongueur2 = tournee.getChemins().size();
        assertTrue(cheminsApresSuppressionLongueur2 < cheminsAvantLongueur, "La tournée doit avoir perdu des chemins après suppression.");
        afficherTournee(tournee);

        // --- Annulation 1 ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION 1--------------");
        controleur.annulerCommande();

        List<Chemin> cheminsApresAnnulation1 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        //assertEquals(cheminsAvant, cheminsApresAnnulation1, "Après 1ère annulation, la tournée doit revenir à son état initial.");
        afficherTournee(tournee);

        // --- Annulation 2 ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION 2--------------");
        controleur.annulerCommande();

        List<Chemin> cheminsApresAnnulation2 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

       // assertEquals(cheminsAvant, cheminsApresAnnulation2, "Après 2e annulation, la tournée doit encore être à son état initial.");
        afficherTournee(tournee);

        // --- Restauration ---
        System.out.print("\n");
        System.out.println("---------------RESTAURATION--------------");
        controleur.restaurerCommande();

        List<Chemin> cheminsApresRestauration2 = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

       // assertEquals(cheminsApresSuppression2, cheminsApresRestauration2, "Après restauration, la suppression doit être réappliquée.");
        afficherTournee(tournee);

        // Vérifie la cohérence de l’instance
       // assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
    }


    @Test
    void testDeuxSuppressionsAvecAnnulationsEtRestaurations() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();
        Tournee tourneeEtat = etat.getTournee();

        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        System.out.println("=== ÉTAT INITIAL ===");
        afficherTournee(tournee);

        int cheminsInitiaux = tournee.getChemins().size();

        // ---------- SUPPRESSION 1 ----------
        System.out.println("\n--- SUPPRESSION 1 ---");
        Map<String, Object> suppr1 = new HashMap<>();
        suppr1.put("idNoeudPickup", 1679901320L);
        suppr1.put("idNoeudDelivery", 208769457L);

        etat.modifierTournee(controleur, "supprimer", suppr1, 4.1);
        int cheminsApresSupp1 = tournee.getChemins().size();
        assertTrue(cheminsApresSupp1 < cheminsInitiaux, "Suppression 1 : la tournée doit avoir perdu des chemins.");
        afficherTournee(tournee);

        // ---------- SUPPRESSION 2 ----------
        System.out.println("\n--- SUPPRESSION 2 ---");
        Map<String, Object> suppr2 = new HashMap<>();
        suppr2.put("idNoeudPickup", 208769120L);
        suppr2.put("idNoeudDelivery", 25336179L);

        etat.modifierTournee(controleur, "supprimer", suppr2, 4.1);
        int cheminsApresSupp2 = tournee.getChemins().size();
        assertTrue(cheminsApresSupp2 < cheminsApresSupp1, "Suppression 2 : encore moins de chemins après la deuxième suppression.");
        afficherTournee(tournee);

        // ---------- ANNULATION 1 ----------
        System.out.println("\n--- ANNULATION 1 ---");
        controleur.annulerCommande();
        int cheminsApresAnnulation1 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp1, cheminsApresAnnulation1,
                "Annulation 1 : on doit revenir à l’état après la première suppression.");
        afficherTournee(tournee);

        // ---------- ANNULATION 2 ----------
        System.out.println("\n--- ANNULATION 2 ---");
        controleur.annulerCommande();
        int cheminsApresAnnulation2 = tournee.getChemins().size();
        assertEquals(cheminsInitiaux, cheminsApresAnnulation2,
                "Annulation 2 : on doit revenir à la tournée initiale.");
        afficherTournee(tournee);

        // ---------- RESTAURATION 1 ----------
        System.out.println("\n--- RESTAURATION 1 ---");
        controleur.restaurerCommande();
        int cheminsApresRestauration1 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp1, cheminsApresRestauration1,
                "Restauration 1 : on doit retrouver l’état après la première suppression.");
        afficherTournee(tournee);

        // ---------- RESTAURATION 2 ----------
        System.out.println("\n--- RESTAURATION 2 ---");
        controleur.restaurerCommande();
        int cheminsApresRestauration2 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp2, cheminsApresRestauration2,
                "Restauration 2 : on doit retrouver l’état après les deux suppressions.");
        afficherTournee(tournee);

        // Vérifie la cohérence globale
        assertSame(tournee, etat.getTournee(), "La même instance de tournée doit être utilisée tout au long.");
    }


    @Test
    void testAjoutAnnulationRestaurationSurMemeTournee2() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();
        Tournee tourneeEtat = etat.getTournee();

        // Vérifie qu'on a bien la même référence au départ
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        // --- copie profonde avant toute modification ---
        List<Chemin> cheminsAvant = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsAvantLongueur = tournee.getChemins().size();
        afficherTournee(tournee);

        // --- Ajout ---
        System.out.print("\n");
        System.out.println("---------------AJOUT--------------");
        Map<String, Object> body = new HashMap<>();
        body.put("idNoeudPickup", 459797860);
        body.put("idNoeudDelivery", 55475018);
        body.put("idPrecedentPickup", tournee.getChemins().get(2).getNoeudDePassageArrivee().getId());
        body.put("idPrecedentDelivery", tournee.getChemins().get(4).getNoeudDePassageDepart().getId());
        body.put("dureeEnlevement", 300.0);
        body.put("dureeLivraison", 300.0);

        etat.modifierTournee(controleur, "ajouter", body, 4.1);

        // --- copie profonde après ajout ---
        List<Chemin> cheminsApresAjout = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresAjoutLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresAjoutLongueur > cheminsAvantLongueur, "La tournée doit avoir plus de chemins après ajout.");
        afficherTournee(tournee);

        // --- Annulation ---
        System.out.print("\n");
        System.out.println("---------------ANNULATION--------------");
        controleur.annulerCommande();

        List<Chemin> cheminsApresAnnulation = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresAnnulationLongueur = tournee.getChemins().size();
        assertEquals(cheminsAvantLongueur, cheminsApresAnnulationLongueur, "Après annulation, la tournée doit retrouver son état initial.");
        assertEquals(cheminsAvant, cheminsApresAnnulation, "Après annulation, les chemins doivent correspondre à l'état initial.");
        afficherTournee(tournee);

        // --- Restauration ---
        System.out.print("\n");
        System.out.println("---------------RESTAURATION--------------");
        controleur.restaurerCommande();

        List<Chemin> cheminsApresRestauration = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresRestaurationLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresRestaurationLongueur > cheminsAvantLongueur, "Après restauration, l’ajout doit être réappliqué.");
        assertEquals(cheminsApresAjout, cheminsApresRestauration, "Après restauration, les chemins doivent correspondre à l'état après ajout.");

        // Vérifie que toutes les opérations ont agi sur la même instance
        assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
        afficherTournee(tournee);
    }

    @Test
    void testSuppressionAjoutAnnulationRestaurationSurMemeTournee() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getCurrentState();
        Tournee tourneeEtat = etat.getTournee();

        // Vérifie qu'on a bien la même référence au départ
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        // --- copie profonde avant toute modification ---
        List<Chemin> cheminsAvant = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        afficherTournee(tournee);

        // ---------- SUPPRESSION ----------
        System.out.println("\n--- SUPPRESSION ---");
        Map<String, Object> suppr = new HashMap<>();
        suppr.put("idNoeudPickup", 208769457L);
        suppr.put("idNoeudDelivery", 1679901320L);
        etat.modifierTournee(controleur, "supprimer", suppr, 4.1);

        List<Chemin> cheminsApresSuppression = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        afficherTournee(tournee);

        // ---------- AJOUT ----------
        System.out.println("\n--- AJOUT ---");
        Map<String, Object> ajout = new HashMap<>();
        ajout.put("idNoeudPickup", 459797860L);
        ajout.put("idNoeudDelivery", 55475018L);
        ajout.put("idPrecedentPickup", tournee.getChemins().get(1).getNoeudDePassageArrivee().getId());
        ajout.put("idPrecedentDelivery", tournee.getChemins().get(2).getNoeudDePassageDepart().getId());
        ajout.put("dureeEnlevement", 300.0);
        ajout.put("dureeLivraison", 300.0);
        etat.modifierTournee(controleur, "ajouter", ajout, 4.1);

        List<Chemin> cheminsApresAjout = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        afficherTournee(tournee);

        // ---------- ANNULATION 1 (annule l'ajout) ----------
        System.out.println("\n--- ANNULATION 1 ---");
        controleur.annulerCommande();
        List<Chemin> cheminsApresAnnulation1 = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        assertEquals(cheminsApresSuppression, cheminsApresAnnulation1, "Après annulation 1, l’état doit correspondre à l’après suppression.");
        afficherTournee(tournee);

        // ---------- ANNULATION 2 (annule la suppression) ----------
        System.out.println("\n--- ANNULATION 2 ---");
        controleur.annulerCommande();
        List<Chemin> cheminsApresAnnulation2 = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        assertEquals(cheminsAvant, cheminsApresAnnulation2, "Après annulation 2, l’état doit correspondre à l’état initial.");
        afficherTournee(tournee);

        // ---------- RESTAURATION 1 (refait la suppression) ----------
        System.out.println("\n--- RESTAURATION 1 ---");
        controleur.restaurerCommande();
        List<Chemin> cheminsApresRestauration1 = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        assertEquals(cheminsApresSuppression, cheminsApresRestauration1, "Après restauration 1, l’état doit correspondre à l’après suppression.");
        afficherTournee(tournee);

        // ---------- RESTAURATION 2 (refait l'ajout) ----------
        System.out.println("\n--- RESTAURATION 2 ---");
        controleur.restaurerCommande();
        List<Chemin> cheminsApresRestauration2 = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();
        assertEquals(cheminsApresAjout, cheminsApresRestauration2, "Après restauration 2, l’état doit correspondre à l’état après ajout.");
        afficherTournee(tournee);

        // Vérifie que toutes les opérations ont agi sur la même instance
        assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
    }



    private static final DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void afficherTournee(Tournee tournee) throws Exception {
        if (tournee == null) {
            System.out.println("La tournée est nulle !");
            return;
        }

        Livreur livreur = tournee.getLivreur();

        System.out.printf("=== Tournée livreur %d ===\n", livreur.getId());


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
