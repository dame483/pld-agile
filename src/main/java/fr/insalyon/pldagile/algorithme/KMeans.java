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
        if (livraisons.isEmpty() || k <= 0) return Collections.emptyList();

        // Étape 1 : Choisir les centres initiaux espacés
        List<Livraison> centresLivraisons = choisirCentresEspaces();

        // Étape 2 : Calcul du barycentre global
        double[] barycentre = calculerBarycentre(centresLivraisons);

        // Étape 3 : Créer les clusters vides
        List<List<Livraison>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

        // Étape 4 : Taille max des clusters (arrondie à la borne supérieure)
        int tailleMaxCluster = (int) Math.ceil((double) livraisons.size() / (double) k);

        // Étape 3 : Trier les livraisons selon la distance au barycentre (ordre décroissant)
        List<Livraison> livraisonsTriees = new ArrayList<>(livraisons);
        livraisonsTriees.sort((a, b) -> {
            double distA = distance(a.getAdresseEnlevement(), barycentre);
            double distB = distance(b.getAdresseEnlevement(), barycentre);
            return Double.compare(distB, distA);
        });

        // Étape 4 : Attribution des livraisons
        for (Livraison l : livraisonsTriees) {
            // Calcul des distances de cette livraison à tous les centres
            List<Integer> ordreClusters = new ArrayList<>();
            for (int i = 0; i < k; i++) ordreClusters.add(i);

            ordreClusters.sort(Comparator.comparingDouble(i ->
                    distance(l.getAdresseEnlevement(), centresLivraisons.get(i).getAdresseEnlevement()))
            );

            // Ajout dans le premier cluster disponible selon l’ordre de proximité
            boolean ajoute = false;
            for (int clusterIdx : ordreClusters) {
                if (clusters.get(clusterIdx).size() < tailleMaxCluster) {
                    clusters.get(clusterIdx).add(l);
                    ajoute = true;
                    break;
                }
            }
        }

        return clusters;
    }


    //  Sélection des centres espacés

    private List<Livraison> choisirCentresEspaces() {
        List<Livraison> centres = new ArrayList<>();
        Random rand = new Random();

        // Premier centre choisi aléatoirement
        Livraison premier = livraisons.get(rand.nextInt(livraisons.size()));
        centres.add(premier);

        // Choisir les suivants comme les plus éloignés des précédents
        while (centres.size() < k) {
            Livraison plusLoin = null;
            double maxDistMoyenne = -1;

            for (Livraison l : livraisons) {
                if (centres.contains(l)) continue;

                double distMoy = 0;
                for (Livraison c : centres) {
                    distMoy += distance(l.getAdresseEnlevement(), c.getAdresseEnlevement());
                }
                distMoy /= centres.size();

                if (distMoy > maxDistMoyenne) {
                    maxDistMoyenne = distMoy;
                    plusLoin = l;
                }
            }

            if (plusLoin != null) centres.add(plusLoin);
            else break;
        }

        return centres;
    }

    //  Calcul du barycentre global

    private double[] calculerBarycentre(List<Livraison> centres) {
        double sumLat = 0, sumLon = 0;
        for (Livraison l : centres) {
            sumLat += l.getAdresseEnlevement().getLatitude();
            sumLon += l.getAdresseEnlevement().getLongitude();
        }
        return new double[]{sumLat / centres.size(), sumLon / centres.size()};
    }

    // Distances euclidiennes

    private double distance(NoeudDePassage n, double[] point) {
        double dx = n.getLatitude() - point[0];
        double dy = n.getLongitude() - point[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double distance(NoeudDePassage n1, NoeudDePassage n2) {
        double dx = n1.getLatitude() - n2.getLatitude();
        double dy = n1.getLongitude() - n2.getLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }
}