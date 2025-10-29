package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

public class TSPHeuristiquePrecedence {

    private final List<NoeudDePassage> noeuds;
    private final Graphe g;
    private final Map<Integer, Integer> precedences; // delivery -> pickup
    private List<Integer> solution; // solution calculée

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
     * Calcule une tournée heuristique itérative à partir d'un sommet de départ
     */
    public void resoudre(int sommetDepart) {
        List<Integer> tournee = new ArrayList<>();
        Set<Integer> nonVus = new HashSet<>();
        for (int i = 0; i < noeuds.size(); i++) {
            if (i != sommetDepart) nonVus.add(i);
        }

        int courant = sommetDepart;
        tournee.add(courant);

        while (!nonVus.isEmpty()) {
            int prochain = -1;
            double minDist = Double.MAX_VALUE;

            for (int v : nonVus) {
                Integer pickup = precedences.get(v);
                if (pickup != null && nonVus.contains(pickup)) continue; // pickup non effectué

                double d = g.getCout(courant, v);
                if (d < minDist) {
                    minDist = d;
                    prochain = v;
                }
            }

            if (prochain == -1) {
                throw new IllegalStateException("Impossible de continuer la tournée heuristique !");
            }

            tournee.add(prochain);
            nonVus.remove(prochain);
            courant = prochain;
        }

        // Retour à l'entrepôt si nécessaire
        if (sommetDepart != courant) tournee.add(sommetDepart);

        this.solution = tournee;
    }

    /**
     * Retourne la solution calculée
     */
    public List<Integer> getSolution() {
        return solution;
    }
}
