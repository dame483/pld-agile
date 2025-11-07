package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.Livraison;
import fr.insalyon.pldagile.modele.NoeudDePassage;

import java.util.*;

/**
 * Classe implementant un TSP (Traveling Salesman Problem) avec contraintes de precedence.
 * <p>
 * Chaque livraison possède un point de retrait (pickup) et un point de livraison (delivery).
 * Cette classe garantit que les livraisons ne peuvent être effectuees qu'après leur retrait.
 * </p>
 * <p>
 * Herite de {@link TemplateTSP} pour utiliser la methode generique de branch-and-bound.
 * </p>
 */

public class TSPAvecPrecedence extends TemplateTSP {

    private final Map<Integer, Integer> precedences; // delivery -> pickup
    private final List<NoeudDePassage> noeuds;

    /**
     * Constructeur.
     *
     * @param noeuds      La liste des noeuds de passage
     * @param livraisons  La liste des livraisons avec pickup et delivery
     * @param g           Le graphe contenant les coûts entre les noeuds
     * @throws IllegalArgumentException Si un pickup ou delivery n'est pas present dans la liste des noeuds
     */
    public TSPAvecPrecedence(List<NoeudDePassage> noeuds, List<Livraison> livraisons, Graphe g) {
        super(g);
        this.noeuds = noeuds;
        this.precedences = new HashMap<>();

        // Pour chaque livraison, le delivery depend du pickup
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
     * Calcule une borne inferieure simple pour le branch-and-bound.
     * <p>
     * Elle retourne le coût minimum possible pour atteindre tous les sommets non encore visites
     * en multipliant le plus petit arc du graphe par le nombre de sommets restants.
     * </p>
     *
     * @param sommetCourant Sommet courant dans le parcours
     * @param nonVus        Ensemble des indices des sommets non encore visites
     * @return Borne inferieure du coût restant pour completer le TSP
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
     * Retourne un iterateur sur les sommets accessibles depuis le sommet courant
     * en respectant les contraintes de pickup → delivery.
     *
     * @param sommetCrt Sommet courant
     * @param nonVus    Sommets non encore visites
     * @param g         Le graphe contenant les coûts
     * @return Iterator des sommets accessibles tries par coût croissant depuis le sommet courant
     */
    @Override
    protected Iterator<Integer> iterator(Integer sommetCrt, Collection<Integer> nonVus, Graphe g) {
        List<Integer> candidats = new ArrayList<>();

        for (Integer v : nonVus) {
            Integer pickup = precedences.get(v);
            // Si v est un delivery et que son pickup n’a pas encore ete visite, on ne peut pas y aller
            if (pickup == null || !nonVus.contains(pickup)) {
                candidats.add(v);
            }
        }

        // Tri des candidats par coût croissant depuis le sommet courant
        candidats.sort(Comparator.comparingDouble(v -> g.getCout(sommetCrt, v)));

        return candidats.iterator();
    }
}
