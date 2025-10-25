package fr.insalyon.pldagile;

import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.algorithme.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CalculTourneeTests {

    // prévoir le cas du non bidirecionnelle
    // faire une petites carte avec quelques troncons écris moi-même et vérifier les matrices
    // prendre la version de malak et créer une branche : optimiser son warshall et renommer
    // vérifier que les tests passent
    // tester le calcul de tournée et TSP1
    // vérifier les cas d'erreurs mardi
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

        //FloydWarshall
        FloydWarshall fw = new FloydWarshall(carte);
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
        // Chemin 1→3 doit contenir 2 tronçons : (1-2) + (2-3)
        Chemin chemin13 = chemins[0][2];
        assertNotNull(chemin13);
        assertEquals(2, chemin13.getTroncons().size());
        assertEquals(15.0, chemin13.getLongueurTotal(), 1e-6);
        assertEquals(1L, chemin13.getNoeudDePassageDepart().getId());
        assertEquals(3L, chemin13.getNoeudDePassageArrivee().getId());

        // Chemin 1→2 = 1 tronçon
        Chemin chemin12 = chemins[0][1];
        assertEquals(1, chemin12.getTroncons().size());
        assertEquals(10.0, chemin12.getLongueurTotal(), 1e-6);

        // Chemin 2→3 = 1 tronçon
        Chemin chemin23 = chemins[1][2];
        assertEquals(1, chemin23.getTroncons().size());
        assertEquals(5.0, chemin23.getLongueurTotal(), 1e-6);


        // Chemin 3→1 = inexistant
        Chemin chemin31 = chemins[2][0]; // indices 2 et 0 pour 3->1
        assertNull(chemin31); // si votre implémentation crée un Chemin avec INF


    }

    /*
        @Test
        void testTSPAvecPrecedence_surPetitGraphe() {

            NoeudDePassage entrepot = new NoeudDePassage(0L, 0, 0, null, 0, null);
            NoeudDePassage pickup1 = new NoeudDePassage(3L, 1, 1, NoeudDePassage.TypeNoeud.PICKUP, 0, null);
            NoeudDePassage delivery1 = new NoeudDePassage(1L, 2, 2, NoeudDePassage.TypeNoeud.DELIVERY, 0, null);
            NoeudDePassage pickup2 = new NoeudDePassage(4L, 3, 3, NoeudDePassage.TypeNoeud.PICKUP, 0, null);
            NoeudDePassage delivery2 = new NoeudDePassage(2L, 4, 4, NoeudDePassage.TypeNoeud.DELIVERY, 0, null);

            List<NoeudDePassage> noeuds = Arrays.asList(entrepot, pickup1, delivery1, pickup2, delivery2);

            // Création des livraisons
            Livraison l1 = new Livraison(pickup1, delivery1);
            Livraison l2 = new Livraison(pickup2, delivery2);
            List<Livraison> livraisons = Arrays.asList(l1, l2);

            // Graphe complet avec les coûts
            int n = noeuds.size();
            GrapheComplet g = new GrapheComplet(n);

            // Initialisation avec INFINITY (indices 0 à n-1)
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        g.setCout(i, j, Double.POSITIVE_INFINITY);
                    }
                }
            }

            // Définition des coûts (indices 0 à n-1)
            g.setCout(0, 1, 1);    // 1->2
            g.setCout(0, 2, 5);    // 1->3
            g.setCout(0, 3, 2);    // 1->4
            g.setCout(0, 4, 10);   // 1->5
            g.setCout(1, 0, 20);   // 2->1
            g.setCout(1, 2, 8);    // 2->3
            g.setCout(1, 3, 5);    // 2->4
            g.setCout(1, 4, 11);   // 2->5
            g.setCout(2, 0, 8);    // 3->1
            g.setCout(2, 1, 7);    // 3->2
            g.setCout(2, 3, 6);    // 3->4
            g.setCout(2, 4, 4);    // 3->5
            g.setCout(3, 0, 3);    // 4->1
            g.setCout(3, 1, 15);   // 4->2
            g.setCout(3, 2, 5);    // 4->3
            g.setCout(3, 4, 8);    // 4->5
            g.setCout(4, 0, 15);   // 5->1
            g.setCout(4, 1, 7);    // 5->2
            g.setCout(4, 2, 6);    // 5->3
            g.setCout(4, 3, 9);    // 5->4

            // Affichage de l'entête (labels 1 à n pour l'utilisateur)
            System.out.print("    ");
            for (int j = 0; j < n; j++) {
                System.out.printf("%6d", j);
            }
            System.out.println();

            // Affichage de la matrice (accès avec indices 0 à n-1)
            for (int i = 0; i < n; i++) {
                System.out.printf("%3d ", i); // label 1 à n pour l'utilisateur
                for (int j = 0; j < n; j++) {
                    double cout = g.getCout(i, j);
                    if (cout == Double.POSITIVE_INFINITY) {
                        System.out.printf("%6s", "INF");
                    } else {
                        System.out.printf("%6.1f", cout);
                    }
                }
                System.out.println();
            }



            // TSP avec précédences
            TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
            tsp.resoudre(0); // départ depuis l'entrepôt

            List<Integer> solution = tsp.getSolution(0);
            if (solution != null) {
                System.out.print("Chemin trouvé : ");
                for (int idx : solution) {
                    System.out.print(idx + " "); // affiche les indices des nœuds
                }
                System.out.println();
            }

            assertNotNull(solution, "Le TSP doit renvoyer une solution");
            assertFalse(solution.isEmpty(), "La solution ne doit pas être vide");

            // Vérification que le chemin choisi correspond au plus court valide
            List<Integer> cheminAttendu = Arrays.asList(0, 1, 2, 3, 4); // correspond à 0→1→2→3→4→0
            assertEquals(cheminAttendu, solution, "Le TSP doit choisir le chemin le plus court respectant les précédences");

            // Optionnel : calculer la distance totale et vérifier
            double distanceTotale = 0;
            for (int i = 0; i < solution.size() - 1; i++) {
                distanceTotale += g.getCout(solution.get(i), solution.get(i + 1));
            }
            distanceTotale += g.getCout(solution.get(solution.size() - 1), 0); // retour à l'entrepôt
            assertEquals(10 + 5 + 6 + 7 + 2, distanceTotale, 1e-6, "La distance totale doit correspondre au plus court chemin valide");
        }
    */
    @Test
    void testTSPAvecPrecedence_surPetitGraphe() throws Exception {
//  Charger la carte
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        System.out.println("Carte chargée.");

        //  Charger la demande de livraison
        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        System.out.println("Demande de livraison chargée.");
    }
}

