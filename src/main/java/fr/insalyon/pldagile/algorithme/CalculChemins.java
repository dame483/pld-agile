package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

public class CalculChemins {

    private final Carte carte;
    private double[][] distances;
    private Chemin[][] matriceChemins;

    public CalculChemins(Carte carte) {
        this.carte = carte;
    }

    /**
     * Calcule pour chaque paire (i,j) la distance minimale (en mètres)
     * et le chemin (liste de tronçons).
     */
    public void calculerMatrice(List<NoeudDePassage> noeuds) {
        int n = noeuds.size();
        distances = new double[n][n];
        matriceChemins = new Chemin[n][n];

        // Pour chaque noeud de passage comme source
        for (int i = 0; i < n; i++) {
            NoeudDePassage depart = noeuds.get(i);

            // Dijkstra -> map idNoeudDestination -> liste de tronçons
            Map<Long, List<Troncon>> chemins = dijkstra(depart);

            for (int j = 0; j < n; j++) {
                NoeudDePassage arrivee = noeuds.get(j);
                if (i == j) {
                    distances[i][j] = 0.0;
                    matriceChemins[i][j] = new Chemin(new ArrayList<>(), 0.0, depart, arrivee);
                } else {
                    List<Troncon> troncons = chemins.get(arrivee.getId());
                    if (troncons != null) {
                        double longueurTotale = troncons.stream().mapToDouble(Troncon::longueur).sum();
                        distances[i][j] = longueurTotale;
                        matriceChemins[i][j] = new Chemin(troncons, longueurTotale, depart, arrivee);
                    } else {
                        distances[i][j] = Double.POSITIVE_INFINITY;
                        matriceChemins[i][j] = null;
                    }
                }
            }
        }
    }

    public double[][] getDistances() {
        return distances;
    }

    public Chemin[][] getMatriceChemins() {
        return matriceChemins;
    }

    /**
     * Dijkstra modifié : construit directement la liste des tronçons du plus court chemin
     * vers chaque nœud (identifié par son id).
     */
    private Map<Long, List<Troncon>> dijkstra(NoeudDePassage depart) {
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, List<Troncon>> chemins = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        // Initialisation
        for (Long id : carte.getNoeuds().keySet()) {
            dist.put(id, Double.POSITIVE_INFINITY);
        }
        dist.put(depart.getId(), 0.0);
        chemins.put(depart.getId(), new ArrayList<>());

        // File de priorité basée sur la distance
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.add(new long[]{depart.getId(), 0L});

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            long idCourant = current[0];
            if (!visited.add(idCourant)) continue;

            // Parcourt tous les tronçons sortants de la carte
            for (Troncon t : carte.getTroncons()) {
                if (t.getIdOrigine() != idCourant) continue;

                long idVoisin = t.getIdDestination();
                double nvDist = dist.get(idCourant) + t.longueur();

                if (nvDist < dist.get(idVoisin)) {
                    dist.put(idVoisin, nvDist);

                    // Construit le chemin complet jusqu’à ce voisin
                    List<Troncon> nouveauChemin = new ArrayList<>(chemins.getOrDefault(idCourant, new ArrayList<>()));
                    nouveauChemin.add(t);
                    chemins.put(idVoisin, nouveauChemin);

                    pq.add(new long[]{idVoisin, (long) nvDist});
                }
            }
        }

        return chemins;
    }
}
