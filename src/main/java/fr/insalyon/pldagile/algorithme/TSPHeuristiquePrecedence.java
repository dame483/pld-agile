package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.erreurs.exception.TSPTimeoutException;
import fr.insalyon.pldagile.modele.*;

import java.util.*;
/**
 * Classe implementant un TSP (Traveling Salesman Problem) avec contraintes de precedence.
 * <p>
 * Chaque livraison possède un point de retrait (pickup) et un point de livraison (delivery).
 * Cette classe garantit que les livraisons ne peuvent être effectuees qu'après leur retrait.
 * </p>
 */

public class TSPHeuristiquePrecedence {

    private final List<NoeudDePassage> noeuds;
    private final Graphe g;
    private final Map<Integer, Integer> precedences; // delivery -> pickup
    private List<Integer> solution; // solution calculee
    private long timeLimitMillis = 10_000;


    /**
     * Constructeur.
     * @param noeuds La liste des noeuds de passage
     * @param livraisons La liste des livraisons (avec pickup et delivery)
     * @param g Le graphe des coûts entre les noeuds
     */
    public TSPHeuristiquePrecedence(List<NoeudDePassage> noeuds, List<Livraison> livraisons, Graphe g) {
        this.noeuds = noeuds;
        this.g = g;
        this.precedences = new HashMap<>();
        for (Livraison l : livraisons) {
            int pickupIdx = noeuds.indexOf(l.getAdresseEnlevement());
            int deliveryIdx = noeuds.indexOf(l.getAdresseLivraison());
            if (pickupIdx == -1 || deliveryIdx == -1)
                throw new IllegalArgumentException("Pickup ou delivery introuvable !");
            precedences.put(deliveryIdx, pickupIdx);
        }
        this.solution = new ArrayList<>();
    }

    /**
     * Calcule une tournee heuristique à partir d'un sommet de depart.
     * @param sommetDepart L'indice du sommet de depart
     * @throws IllegalStateException Si la tournee ne peut pas être completee
     */
    public void resoudre(int sommetDepart) {
        List<Integer> tournee = new ArrayList<>();
        Set<Integer> nonVus = new HashSet<>();
        for (int i = 0; i < noeuds.size(); i++) {
            if (i != sommetDepart) nonVus.add(i);
        }

        int courant = sommetDepart;
        tournee.add(courant);

        long startTime = System.currentTimeMillis();

        while (!nonVus.isEmpty()) {

            if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                throw new TSPTimeoutException("Le TSPHeuristiquePrecedence a depasse la limite de " + timeLimitMillis + " ms");
            }

            int prochain = -1;
            double minDist = Double.MAX_VALUE;

            for (int v : nonVus) {
                Integer pickup = precedences.get(v);
                if (pickup != null && nonVus.contains(pickup)) continue; // pickup non effectue

                double d = g.getCout(courant, v);
                if (d < minDist) {
                    minDist = d;
                    prochain = v;
                }
            }

            if (prochain == -1) {
                throw new IllegalStateException("Impossible de continuer la tournee heuristique !");
            }

            tournee.add(prochain);
            nonVus.remove(prochain);
            courant = prochain;
        }

        // Retour à l'entrepôt si necessaire
        if (sommetDepart != courant) tournee.add(sommetDepart);

        this.solution = tournee;
    }

    /**
     * Retourne la solution calculee.
     * @return Liste d'indices representant la tournee
     */
    public List<Integer> getSolution() {
        return solution;
    }
}
