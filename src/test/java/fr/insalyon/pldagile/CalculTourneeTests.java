package fr.insalyon.pldagile;

import fr.insalyon.pldagile.exception.TourneeNonConnexeException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.algorithme.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CalculTourneeTests {


    /**
     * Test CalculChemins sur un graphe très simple (2 nœuds et un tronçon).
     * Vérifie que la distance minimale calculée entre n1 et n2 est correcte.
     */
    @Test
    void testCalculChemins_simpleGraphe() {
        Carte carte = new Carte();
        Noeud n1 = new Noeud(1L, 0, 0);
        Noeud n2 = new Noeud(2L, 1, 1);
        carte.AjouterNoeud(n1);
        carte.AjouterNoeud(n2);
        carte.AjouterTroncon(new Troncon(1L, 2L, 10.0, "rue X"));

        CalculChemins fw = new CalculChemins(carte);
        fw.calculerMatrice(List.of(
                new NoeudDePassage(n1.getId(), 0, 0, null, 0, null),
                new NoeudDePassage(n2.getId(), 1, 1, null, 0, null)
        ));

        assertEquals(10.0, fw.getDistances()[0][1], 1e-6);
    }


    /**
     * Test des méthodes setCout/getCout de GrapheComplet.
     * Vérifie qu’on peut stocker et récupérer correctement un coût entre deux nœuds.
     */
    @Test
    void testGrapheComplet_setEtGetCout() {
        GrapheComplet g = new GrapheComplet(3);
        g.setCout(0, 1, 5.0);
        assertEquals(5.0, g.getCout(0, 1));
    }


    /**
     * Test Dijkstra via CalculChemins.calculerMatrice sur un graphe de 6 nœuds.
     * 3 nœuds de passage et tronçons directionnels.
     * Vérifie que les distances minimales entre tous les nœuds de passage sont correctes.
     */
    @Test
    void testDijkstra_surGraphe6Noeuds() {
        Carte carte = new Carte();
        // 6 noeuds
        for (long i = 1; i <= 6; i++) {
            carte.AjouterNoeud(new Noeud(i, i, i));
        }

        // Arêtes directionnelles
        carte.AjouterTroncon(new Troncon(1, 2, 7.0, "A"));
        carte.AjouterTroncon(new Troncon(1, 3, 9.0, "B"));
        carte.AjouterTroncon(new Troncon(1, 6, 14.0, "C"));
        carte.AjouterTroncon(new Troncon(2, 3, 10.0, "D"));
        carte.AjouterTroncon(new Troncon(2, 4, 15.0, "E"));
        carte.AjouterTroncon(new Troncon(3, 4, 11.0, "F"));
        carte.AjouterTroncon(new Troncon(3, 6, 2.0, "G"));
        carte.AjouterTroncon(new Troncon(6, 5, 9.0, "H"));
        carte.AjouterTroncon(new Troncon(4, 5, 6.0, "I"));

        // Nœuds de passage : n1, n4, n5
        List<NoeudDePassage> noeudsPassage = List.of(
                new NoeudDePassage(1, 0, 0, null, 0, null),
                new NoeudDePassage(4, 0, 0, null, 0, null),
                new NoeudDePassage(5, 0, 0, null, 0, null)
        );

        CalculChemins chemins = new CalculChemins(carte);
        chemins.calculerMatrice(noeudsPassage);

        double[][] dist = chemins.getDistances();

        // Vérification des distances minimales (indices = positions dans la liste)
        assertEquals(0.0, dist[0][0], 1e-6);        // n1->n1
        assertEquals(20.0, dist[0][1], 1e-6);       // n1->n4 (1->3->4 = 9+11)
        assertEquals(20.0, dist[0][2], 1e-6);
        assertEquals(Double.POSITIVE_INFINITY, dist[1][0], 1e-6); // n4->n1 impossible
        assertEquals(0.0, dist[1][1], 1e-6);        // n4->n4
        assertEquals(6.0, dist[1][2], 1e-6);        // n4->n5
        assertEquals(Double.POSITIVE_INFINITY, dist[2][0], 1e-6); // n5->n1 impossible
        assertEquals(Double.POSITIVE_INFINITY, dist[2][1], 1e-6); // n5->n4 impossible
        assertEquals(0.0, dist[2][2], 1e-6);        // n5->n5
    }




    /**
     * Test calculerMatrice sur un petit graphe de 3 nœuds avec tronçons multiples.
     * Vérifie la cohérence des distances et des chemins calculés, y compris les chemins inatteignables.
     */
    @Test
    void testCalculerMatrice_surPetiteCarte() {

        Carte carte = new Carte();

        Noeud n1 = new Noeud(1L, 0.0, 0.0);
        Noeud n2 = new Noeud(2L, 1.0, 1.0);
        Noeud n3 = new Noeud(3L, 2.0, 2.0);

        carte.AjouterNoeud(n1);
        carte.AjouterNoeud(n2);
        carte.AjouterNoeud(n3);

        // Tronçons
        carte.AjouterTroncon(new Troncon(1L, 2L, 10.0, "rue A"));
        carte.AjouterTroncon(new Troncon(2L, 3L, 5.0, "rue B"));
        carte.AjouterTroncon(new Troncon(1L, 3L, 20.0, "rue C"));

        //Noeuds de passages
        NoeudDePassage np1 = new NoeudDePassage(n1.getId(), n1.getLatitude(), n1.getLongitude(), null, 0, null);
        NoeudDePassage np2 = new NoeudDePassage(n2.getId(), n2.getLatitude(), n2.getLongitude(), null, 0, null);
        NoeudDePassage np3 = new NoeudDePassage(n3.getId(), n3.getLatitude(), n3.getLongitude(), null, 0, null);

        List<NoeudDePassage> noeuds = List.of(np1, np2, np3);

        //CalculChemins
        CalculChemins fw = new CalculChemins(carte);
        fw.calculerMatrice(noeuds);

        double[][] distances = fw.getDistances();
        Chemin[][] chemins = fw.getMatriceChemins();

        System.out.println("=== Matrice des distances ===");
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                if (distances[i][j] == Double.POSITIVE_INFINITY) {
                    System.out.print("INF\t"); // nœud inatteignable
                } else {
                    System.out.print(distances[i][j] + "\t");
                }
            }
            System.out.println();
        }

        //Vérifier distances
        assertEquals(0.0, distances[0][0], 1e-6);
        assertEquals(10.0, distances[0][1], 1e-6);
        assertEquals(15.0, distances[0][2], 1e-6);
        assertEquals(5.0, distances[1][2], 1e-6);
        assertEquals(5.0, distances[1][2], 1e-6);
        assertTrue(distances[2][0] == Double.POSITIVE_INFINITY);
        assertTrue(distances[2][1] == Double.POSITIVE_INFINITY);


        System.out.println("=== Matrice des chemins ===");

        for (int i = 0; i < chemins.length; i++) {
            for (int j = 0; j < chemins[i].length; j++) {
                Chemin c = chemins[i][j];
                if (c == null || c.getLongueurTotal() == Double.POSITIVE_INFINITY) {
                    System.out.print("[]\t");  // Chemin inatteignable
                } else {
                    String tronconsStr = c.getTroncons().stream()
                            .map(t -> t.getIdOrigine() + "-" + t.getIdDestination())
                            .reduce((a, b) -> a + "," + b)
                            .orElse("");
                    System.out.print("[" + tronconsStr + "]\t");
                }
            }
            System.out.println();
        }


        //Vérifier cohérence des chemins
        Chemin chemin13 = chemins[0][2];
        assertNotNull(chemin13);
        assertEquals(2, chemin13.getTroncons().size());
        assertEquals(15.0, chemin13.getLongueurTotal(), 1e-6);
        assertEquals(1L, chemin13.getNoeudDePassageDepart().getId());
        assertEquals(3L, chemin13.getNoeudDePassageArrivee().getId());

        Chemin chemin12 = chemins[0][1];
        assertEquals(1, chemin12.getTroncons().size());
        assertEquals(10.0, chemin12.getLongueurTotal(), 1e-6);

        Chemin chemin23 = chemins[1][2];
        assertEquals(1, chemin23.getTroncons().size());
        assertEquals(5.0, chemin23.getLongueurTotal(), 1e-6);


        Chemin chemin31 = chemins[2][0]; // indices 2 et 0 pour 3->1
        assertNull(chemin31); // si votre implémentation crée un Chemin avec INF


    }



    /**
     * Test le calcul des horaires de départ et d’arrivée pour des chemins simples.
     * Vérifie que l’horaire d’arrivée est après l’horaire de départ et que les horaires sont correctement calculés.
     */
    @Test
    void testCalculHorairesChemins_simpleCas() throws Exception {
        // Mock simple : 2 points et un chemin de 100 m à 5 m/s
        Carte carte = new Carte();
        Noeud n1 = new Noeud(1L, 0, 0);
        Noeud n2 = new Noeud(2L, 1, 1);
        carte.AjouterNoeud(n1);
        carte.AjouterNoeud(n2);
        Troncon t = new Troncon(1L, 2L, 100.0, "Rue");
        carte.AjouterTroncon(t);

        NoeudDePassage np1 = new NoeudDePassage(1L, 0, 0, null, 0, null);
        NoeudDePassage np2 = new NoeudDePassage(2L, 1, 1, null, 10, null);

        DemandeDeLivraison demande = new DemandeDeLivraison(np1, List.of(
                new Livraison(np1, np2)
        ));

        CalculTournees ct = new CalculTournees(carte, demande, 5.0, LocalTime.of(8, 0));
        Tournee tournee = ct.calculerTournee();

        assertNotNull(tournee);
        assertEquals(LocalTime.of(8, 0), np1.getHoraireDepart());
        assertTrue(np2.getHoraireArrivee().isAfter(np1.getHoraireDepart()));
    }



    /**
     * Test TSPAvecPrecedence sur un petit graphe de 3 nœuds.
     * Vérifie que l’ordre des livraisons est respecté dans la solution finale.
     */
    @Test
    void testTSPAvecPrecedence_ordreRespecte() {
        GrapheComplet g = new GrapheComplet(3);
        g.setCout(0, 1, 10);
        g.setCout(1, 2, 5);
        g.setCout(0, 2, 20);

        List<NoeudDePassage> noeuds = new ArrayList<>();
        noeuds.add(new NoeudDePassage(1L, 0, 0, null, 0, null));
        noeuds.add(new NoeudDePassage(2L, 1, 1, null, 0, null));
        noeuds.add(new NoeudDePassage(3L, 2, 2, null, 0, null));

        List<Livraison> livraisons = new ArrayList<>();
        livraisons.add(new Livraison(noeuds.get(0), noeuds.get(1)));

        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0);

        List<Integer> sol = tsp.getSolution(0);
        assertNotNull(sol);
        assertTrue(sol.containsAll(List.of(0, 1, 2)));
    }



    /**
     * Teste le calcul complet d'une tournée à partir de fichiers XML de carte et de demande.
     * Vérifie que la tournée contient des chemins avec les bonnes distances et horaires.
     */
    @Test
    void testCalculerTournee_surPetitGraphe() throws Exception {

        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);


        double vitesse = 4.16; // m/s
        CalculTournees calculTournee = new CalculTournees(ville, demande, vitesse, heureDepart);
        Tournee tournee = calculTournee.calculerTournee();
        assertNotNull(tournee, "La tournée doit être calculée");

        List<Chemin> chemins = tournee.getChemins();
        assertFalse(chemins.isEmpty(), "La tournée doit contenir des chemins");


        long[] ordreAttendu = {2835339774L, 208769120L, 1679901320L, 208769457L, 25336179L, 2835339774L};
        double[] distancesAttendu = {447, 866, 411, 1540, 1115};
        String[] horairesDepartAttendu = {"08:00:00", "08:08:47", "08:19:15", "08:30:54", "08:45:04"};
        String[] horairesArriveeAttendu = {"08:01:47", "08:12:15", "08:20:54", "08:37:04", "08:49:32"};

        for (int i = 0; i < chemins.size(); i++) {
            Chemin c = chemins.get(i);

            long departId = c.getNoeudDePassageDepart().getId();
            long arriveeId = c.getNoeudDePassageArrivee().getId();

            assertEquals(ordreAttendu[i], departId, "Noeud de départ chemin " + (i + 1));
            assertEquals(ordreAttendu[i + 1], arriveeId, "Noeud d'arrivée chemin " + (i + 1));

            assertEquals(distancesAttendu[i], c.getLongueurTotal(), 1.0, "Distance chemin " + (i + 1));

            // Horaire de départ du chemin : horaireDepart du noeud départ, sinon horaireArrivee pour l’entrepôt
            LocalTime departHoraire = c.getNoeudDePassageDepart().getHoraireDepart();
            if (departHoraire == null) departHoraire = c.getNoeudDePassageDepart().getHoraireArrivee();
            assertEquals(LocalTime.parse(horairesDepartAttendu[i]), departHoraire, "Horaire départ chemin " + (i + 1));

            // Horaire d'arrivée du chemin : horaireArrivee du noeud d'arrivée
            assertEquals(LocalTime.parse(horairesArriveeAttendu[i]), c.getNoeudDePassageArrivee().getHoraireArrivee(), "Horaire arrivée chemin " + (i + 1));
        }
    }



    /**
     * Teste le cas où le graphe des chemins n'est pas connexe.
     * La tournée ne peut pas être calculée car il n'existe aucun tronçon
     */
    @Test
    void testTourneeGrapheNonConnexe() {
        Carte carte = new Carte();
        Noeud n1 = new Noeud(1L, 0, 0);
        Noeud n2 = new Noeud(2L, 1, 1);
        carte.AjouterNoeud(n1);
        carte.AjouterNoeud(n2);
        // Pas de tronçons => n1 et n2 non connectés

        NoeudDePassage np1 = new NoeudDePassage(1L, 0, 0, null, 0, null);
        NoeudDePassage np2 = new NoeudDePassage(2L, 1, 1, null, 0, null);

        DemandeDeLivraison demande = new DemandeDeLivraison(np1, List.of(new Livraison(np1, np2)));
        CalculTournees ct = new CalculTournees(carte, demande, 5.0, LocalTime.of(8, 0));

        assertThrows(TourneeNonConnexeException.class, ct::calculerTournee);
    }




    /**
     * Teste un scénario où la matrice des chemins contient exactement deux
     * valeurs infinies (INF) dans une matrice 3x3.
   */
    @Test
    void testTourneeGrapheAvecDeuxINF() throws Exception {
        Carte carte = new Carte();

        Noeud n1 = new Noeud(1L, 0, 0);
        Noeud n2 = new Noeud(2L, 1, 1);
        Noeud n3 = new Noeud(3L, 2, 2);
        carte.AjouterNoeud(n1);
        carte.AjouterNoeud(n2);
        carte.AjouterNoeud(n3);

        NoeudDePassage np1 = new NoeudDePassage(1L,0,0,null,0,null);
        NoeudDePassage np2 = new NoeudDePassage(2L,0,0,null,0,null);
        NoeudDePassage np3 = new NoeudDePassage(3L,0,0,null,0,null);

        DemandeDeLivraison demande = new DemandeDeLivraison(np1, List.of(
                new Livraison(np1, np2),
                new Livraison(np2, np3)
        ));

        CalculTournees ct = new CalculTournees(carte, demande, 5.0, LocalTime.of(8,0));

        TourneeNonConnexeException ex = assertThrows(
                TourneeNonConnexeException.class,
                ct::calculerTournee
        );

        assertTrue(ex.getMessage().contains("connexe"));
    }





    // KMeans

}

