package fr.insalyon.pldagile;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.controleur.*;
import fr.insalyon.pldagile.modele.*;
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

    private Controleur controleur;
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

        controleur = new Controleur();

        EtatModificationTournee etat = new EtatModificationTournee(carte, tournee);
        controleur.setEtatActuelle(etat);
    }




    @Test
    void testDeuxSuppressionsAvecAnnulationsEtRestaurations() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getEtatActuelle();
        Tournee tourneeEtat = etat.getTournee();

        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        int cheminsInitiaux = tournee.getChemins().size();

        // ---------- SUPPRESSION 1 ----------
        Map<String, Object> suppr1 = new HashMap<>();
        suppr1.put("idNoeudPickup", 1679901320L);
        suppr1.put("idNoeudDelivery", 208769457L);

        etat.modifierTournee(controleur, "supprimer", suppr1, 4.1);
        int cheminsApresSupp1 = tournee.getChemins().size();
        assertTrue(cheminsApresSupp1 < cheminsInitiaux, "Suppression 1 : la tournée doit avoir perdu des chemins.");

        // ---------- SUPPRESSION 2 ----------
        Map<String, Object> suppr2 = new HashMap<>();
        suppr2.put("idNoeudPickup", 208769120L);
        suppr2.put("idNoeudDelivery", 25336179L);

        etat.modifierTournee(controleur, "supprimer", suppr2, 4.1);
        int cheminsApresSupp2 = tournee.getChemins().size();
        assertTrue(cheminsApresSupp2 < cheminsApresSupp1, "Suppression 2 : encore moins de chemins après la deuxième suppression.");

        // ---------- ANNULATION 1 ----------
        controleur.annulerCommande();
        int cheminsApresAnnulation1 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp1, cheminsApresAnnulation1,
                "Annulation 1 : on doit revenir à l’état après la première suppression.");

        // ---------- ANNULATION 2 ----------
        controleur.annulerCommande();
        int cheminsApresAnnulation2 = tournee.getChemins().size();
        assertEquals(cheminsInitiaux, cheminsApresAnnulation2,
                "Annulation 2 : on doit revenir à la tournée initiale.");

        // ---------- RESTAURATION 1 ----------
        controleur.restaurerCommande();
        int cheminsApresRestauration1 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp1, cheminsApresRestauration1,
                "Restauration 1 : on doit retrouver l’état après la première suppression.");

        // ---------- RESTAURATION 2 ----------
        controleur.restaurerCommande();
        int cheminsApresRestauration2 = tournee.getChemins().size();
        assertEquals(cheminsApresSupp2, cheminsApresRestauration2,
                "Restauration 2 : on doit retrouver l’état après les deux suppressions.");

        // Vérifie la cohérence globale
        assertSame(tournee, etat.getTournee(), "La même instance de tournée doit être utilisée tout au long.");
    }


    @Test
    void testAjoutAnnulationRestaurationSurMemeTournee2() throws Exception {
        EtatModificationTournee etat = (EtatModificationTournee) controleur.getEtatActuelle();
        Tournee tourneeEtat = etat.getTournee();

        // Vérifie qu'on a bien la même référence au départ
        assertSame(tournee, tourneeEtat, "L’état doit référencer la même tournée que le test.");

        // --- copie profonde avant toute modification ---
        List<Chemin> cheminsAvant = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsAvantLongueur = tournee.getChemins().size();

        // --- Ajout ---
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

        // --- Annulation ---
        controleur.annulerCommande();

        List<Chemin> cheminsApresAnnulation = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresAnnulationLongueur = tournee.getChemins().size();
        assertEquals(cheminsAvantLongueur, cheminsApresAnnulationLongueur, "Après annulation, la tournée doit retrouver son état initial.");
        assertEquals(cheminsAvant, cheminsApresAnnulation, "Après annulation, les chemins doivent correspondre à l'état initial.");

        // --- Restauration ---
        controleur.restaurerCommande();

        List<Chemin> cheminsApresRestauration = new ArrayList<>(tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList());

        int cheminsApresRestaurationLongueur = tournee.getChemins().size();
        assertTrue(cheminsApresRestaurationLongueur > cheminsAvantLongueur, "Après restauration, l’ajout doit être réappliqué.");
        assertEquals(cheminsApresAjout, cheminsApresRestauration, "Après restauration, les chemins doivent correspondre à l'état après ajout.");

        // Vérifie que toutes les opérations ont agi sur la même instance
        assertSame(tournee, etat.getTournee(), "Même instance de tournée utilisée tout au long du test.");
    }


}
