package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.Livraison;
import fr.insalyon.pldagile.modele.NoeudDePassage;

import java.util.*;

public class TSPAvecPrecedence extends TemplateTSP {

    private final Map<Integer, Integer> precedences; // delivery -> pickup
    private final List<NoeudDePassage> noeuds;

    public TSPAvecPrecedence(List<NoeudDePassage> noeuds, List<Livraison> livraisons, Graphe g) {
        super(g);
        this.noeuds = noeuds;
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

    /**
     * Borne inférieure simple pour couper les branches inutiles
     */
    @Override
    protected double bound(Integer sommetCourant, Collection<Integer> nonVus) {
        double minArc = Double.MAX_VALUE;
        for (int i = 0; i < g.getNbSommets(); i++) {
            for (int j = 0; j < g.getNbSommets(); j++) {
                if (i != j && g.estArc(i, j)) {
                    minArc = Math.min(minArc, g.getCout(i, j));
                }
            }
        }
        return (minArc == Double.MAX_VALUE) ? 0 : minArc * nonVus.size();
    }

    /**
     * Itérateur des sommets accessibles respectant la contrainte pickup → delivery
     */
    @Override
    protected Iterator<Integer> iterator(Integer sommetCrt, Collection<Integer> nonVus, Graphe g) {
        List<Integer> candidats = new ArrayList<>();

        for (Integer v : nonVus) {
            Integer pickup = precedences.get(v);
            // Si v est un delivery et que son pickup n’a pas encore été visité, on ne peut pas y aller
            if (pickup == null || !nonVus.contains(pickup)) {
                candidats.add(v);
            }
        }

        // Tri des candidats par coût croissant depuis le sommet courant
        candidats.sort(Comparator.comparingDouble(v -> g.getCout(sommetCrt, v)));

        return candidats.iterator();
    }
}
