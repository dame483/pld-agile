package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

public class KMeans {

    private final List<Livraison> livraisons;
    private final int k;

    public KMeans(List<Livraison> livraisons, int k) {
        this.livraisons = livraisons;
        this.k = k;
    }

    public List<List<Livraison>> cluster() {
        // Initialiser K centres à partir des pickups
        List<double[]> centres = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < k; i++) {
            Livraison l = livraisons.get(rand.nextInt(livraisons.size()));
            centres.add(new double[]{l.getAdresseEnlevement().getLatitude(), l.getAdresseLivraison().getLongitude()});
        }

        boolean converged = false;
        List<List<Livraison>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

        while (!converged) {
            //  Assigner chaque livraison au centre le plus proche
            for (List<Livraison> c : clusters) c.clear();
            for (Livraison l : livraisons) {
                double minDist = Double.MAX_VALUE;
                int bestCluster = 0;
                for (int i = 0; i < centres.size(); i++) {
                    double dist = distance(l.getAdresseEnlevement(), centres.get(i));
                    if (dist < minDist) {
                        minDist = dist;
                        bestCluster = i;
                    }
                }
                clusters.get(bestCluster).add(l);
            }

            // Recalculer les centres à partir des pickups
            boolean changed = false;
            for (int i = 0; i < k; i++) {
                List<Livraison> cluster = clusters.get(i);
                if (cluster.isEmpty()) continue;
                double avgLat = cluster.stream().mapToDouble(l -> l.getAdresseEnlevement().getLatitude()).average().orElse(0);
                double avgLon = cluster.stream().mapToDouble(l -> l.getAdresseEnlevement().getLongitude()).average().orElse(0);
                double[] oldCentre = centres.get(i);
                if (oldCentre[0] != avgLat || oldCentre[1] != avgLon) {
                    centres.set(i, new double[]{avgLat, avgLon});
                    changed = true;
                }
            }

            converged = !changed;
        }

        return clusters;
    }

    private double distance(NoeudDePassage n, double[] centre) {
        double dx = n.getLatitude() - centre[0];
        double dy = n.getLongitude() - centre[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
}
