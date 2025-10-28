package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

public class TSPHeuristique {

    private final List<NoeudDePassage> noeuds;
    private final Graphe g;
    private final Map<Integer, Integer> precedences; // delivery -> pickup

    public TSPHeuristique(List<NoeudDePassage> noeuds,List<Livraison> livraisons,Graphe g) {
        this.noeuds = noeuds;
        this.g = g;
        this.precedences = new HashMap<>();
        // Pour chaque livraison, le delivery dépend du pickup
        for (Livraison l : livraisons) {
            int pickupIdx = noeuds.indexOf(l.getAdresseEnlevement());
            int deliveryIdx = noeuds.indexOf(l.getAdresseLivraison());

            if (pickupIdx == -1 || deliveryIdx == -1) {
                throw new IllegalArgumentException(
                        "Pickup ou delivery introuvable dans la liste des noeuds de passage !"
                );
            }

            precedences.put(deliveryIdx, pickupIdx);
        }
    }

    public List<Integer> calculerTourneeHeuristique(int departIndex) {
        List<Integer> tournee = new ArrayList<>();
        Set<Integer> nonVus = new HashSet<>();
        for (int i = 1; i < noeuds.size(); i++) nonVus.add(i);

        int courant = departIndex;
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


            tournee.add(prochain);
            nonVus.remove(prochain);
            courant = prochain;
        }

        if (departIndex != courant) tournee.add(departIndex);

        return tournee;
    }

}
