package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

/**
 * Classe permettant de calculer les chemins et distances entre plusieurs nœuds
 * sur une carte donnee en utilisant l'algorithme de Dijkstra.
 */
public class CalculChemins {

    /**
     * Carte sur laquelle les chemins sont calcules.
     */
    private final Carte carte;

    /**
     * Matrice des distances entre tous les nœuds calcules.
     */
    private double[][] distances;

    /**
     * Matrice des chemins entre tous les nœuds calcules.
     */
    private Chemin[][] matriceChemins;

    /**
     * Constructeur.
     *
     * @param carte La carte contenant les nœuds et tronçons
     */
    public CalculChemins(Carte carte) {
        this.carte = carte;
    }

    /**
     * Calcule la matrice des distances et la matrice des chemins pour une liste de nœuds.
     *
     * @param noeuds Liste des nœuds de passage pour lesquels calculer les distances et chemins
     */
    public void calculerMatrice(List<NoeudDePassage> noeuds) {
        int n = noeuds.size();
        distances = new double[n][n];
        matriceChemins = new Chemin[n][n];

        for (int i = 0; i < n; i++) {
            NoeudDePassage depart = noeuds.get(i);

            Map<Long, List<Troncon>> chemins = dijkstra(depart);

            for (int j = 0; j < n; j++) {
                NoeudDePassage arrivee = noeuds.get(j);
                if (i == j) {
                    distances[i][j] = 0.0;
                    matriceChemins[i][j] = new Chemin(new ArrayList<>(), 0.0, depart, arrivee);
                } else {
                    List<Troncon> troncons = chemins.get(arrivee.getId());
                    if (troncons != null) {
                        double longueurTotale = troncons.stream().mapToDouble(Troncon::getLongueur).sum();
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

    /**
     * Retourne la matrice des distances entre tous les nœuds.
     *
     * @return Tableau 2D des distances
     */
    public double[][] getDistances() {
        return distances;
    }


    /**
     * Retourne la matrice des chemins entre tous les nœuds.
     *
     * @return Tableau 2D des objets Chemin
     */
    public Chemin[][] getMatriceChemins() {
        return matriceChemins;
    }

    /**
     * Calcule les plus courts chemins depuis un nœud de depart vers tous les autres
     * nœuds de la carte en utilisant l'algorithme de Dijkstra.
     *
     * @param depart Nœud de depart
     * @return Map associant l'identifiant d'un nœud à la liste des tronçons le menant depuis le depart
     */
    private Map<Long, List<Troncon>> dijkstra(NoeudDePassage depart) {
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, List<Troncon>> chemins = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        for (Long id : carte.getNoeuds().keySet()) {
            dist.put(id, Double.POSITIVE_INFINITY);
        }
        dist.put(depart.getId(), 0.0);
        chemins.put(depart.getId(), new ArrayList<>());

        // File de priorite basee sur la distance
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.add(new long[]{depart.getId(), 0L});

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            long idCourant = current[0];
            if (!visited.add(idCourant)) continue;

            for (Troncon t : carte.getTroncons()) {
                if (t.getIdOrigine() != idCourant) continue;

                long idVoisin = t.getIdDestination();
                double nvDist = dist.get(idCourant) + t.getLongueur();

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

    /**
     * Calcule le chemin le plus court entre deux nœuds de passage.
     *
     * @param depart  Nœud de depart
     * @param arrivee Nœud d'arrivee
     * @return Chemin le plus court entre depart et arrivee, ou null si aucun chemin n'existe
     * @throws IllegalArgumentException si depart ou arrivee est null
     */
    public Chemin calculerCheminPlusCourt(NoeudDePassage depart, NoeudDePassage arrivee) {
        if (depart == null || arrivee == null) {
            throw new IllegalArgumentException("Les nœuds de depart et d'arrivee ne peuvent pas être nuls.");
        }

        Map<Long, List<Troncon>> chemins = dijkstra(depart);
        List<Troncon> troncons = chemins.get(arrivee.getId());

        if (troncons == null) {
            return null;
        }

        double longueurTotale = troncons.stream()
                .mapToDouble(Troncon::getLongueur)
                .sum();

        return new Chemin(troncons, longueurTotale, depart, arrivee);
    }


    /**
     * Retourne la carte utilisee pour le calcul des chemins.
     *
     * @return La carte
     */
    public Carte getCarte() {
        return carte;
    }
}
