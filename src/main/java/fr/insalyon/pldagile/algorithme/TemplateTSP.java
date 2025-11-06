package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.erreurs.exception.TSPTimeoutException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class TemplateTSP {

    protected Graphe g;                       // le graphe
    protected List<List<Integer>> solution;    // solution du TSP : liste de listes d'indices de sommets
    private final long timeLimitMillis = 10_000; // 10 secondes
    private long startTime;

    public TemplateTSP(Graphe g) {
        this.g = g;
        this.solution = new ArrayList<>();
    }

    // Methodes abstraites à implementer
    protected abstract double bound(Integer sommetCourant, Collection<Integer> nonVus);
    protected abstract Iterator<Integer> iterator(Integer sommetCourant, Collection<Integer> nonVus, Graphe g);

    // Resolution du TSP
    public void resoudre(int depart) {
        this.startTime = System.currentTimeMillis(); //gerer les  timeout
        List<Integer> current = new ArrayList<>();
        List<Integer> nonVus = new ArrayList<>();
        for (int i = 0; i < g.getNbSommets(); i++) {
            if (i != depart) nonVus.add(i);
        }
        current.add(depart);
        solution.clear();
        branchAndBound(current, nonVus, 0);
    }

    private void branchAndBound(List<Integer> current, List<Integer> nonVus, double coutActuel) {
        if (nonVus.isEmpty()) {
            // solution complète trouvee
            solution.add(new ArrayList<>(current));
            return;
        }

        Iterator<Integer> it = iterator(current.get(current.size() - 1), nonVus, g);
        while (it.hasNext()) {
            if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                throw new TSPTimeoutException("Le TSP a depasse 10 secondes");
            }
            Integer next = it.next();
            current.add(next);
            List<Integer> nextNonVus = new ArrayList<>(nonVus);
            nextNonVus.remove(next);

            branchAndBound(current, nextNonVus, coutActuel + g.getCout(current.get(current.size() - 2), next));
            current.remove(current.size() - 1);
        }
    }

    // Retourne la i-ème solution
    public List<Integer> getSolution(int i) {
        if (i < 0 || i >= solution.size()) return null;
        return solution.get(i);
    }
}
