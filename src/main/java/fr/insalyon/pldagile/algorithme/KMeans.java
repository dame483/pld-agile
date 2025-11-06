package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.*;

/**
 * Classe implementant l'algorithme de clustering K-Means pour les livraisons.
 * <p>
 * Permet de regrouper les livraisons en k clusters en fonction de la proximite geographique
 * de leurs adresses de retrait (pickup).
 * </p>
 */
public class KMeans {
    /**
     * Liste des livraisons à clusteriser.
     */
    private final List<Livraison> livraisons;
    /**
     * Nombre de clusters à creer.
     */
    private final int k;

    /**
     * Constructeur.
     *
     * @param livraisons La liste des livraisons à regrouper
     * @param k          Le nombre de clusters
     */
    public KMeans(List<Livraison> livraisons, int k) {
        this.livraisons = livraisons;
        this.k = k;
    }

    /**
     * Execute l'algorithme de clustering K-Means.
     * <p>
     * Les etapes principales sont :
     * <ul>
     *     <li>Choix des centres initiaux espaces</li>
     *     <li>Calcul du barycentre global</li>
     *     <li>Tri des livraisons par distance au barycentre</li>
     *     <li>Attribution des livraisons aux clusters selon la proximite et la taille maximale des clusters</li>
     * </ul>
     *
     * @return Une liste de k clusters, chaque cluster etant une liste de livraisons
     */
    public List<List<Livraison>> cluster() {
        if (livraisons.isEmpty() || k <= 0) return Collections.emptyList();

        // etape 1 : Choisir les centres initiaux espaces
        List<Livraison> centresLivraisons = choisirCentresEspaces();

        // etape 2 : Calcul du barycentre global
        double[] barycentre = calculerBarycentre(centresLivraisons);

        // etape 3 : Creer les clusters vides
        List<List<Livraison>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

        // etape 4 : Taille max des clusters (arrondie à la borne superieure)
        int tailleMaxCluster = (int) Math.ceil((double) livraisons.size() / (double) k);

        // etape 3 : Trier les livraisons selon la distance au barycentre (ordre decroissant)
        List<Livraison> livraisonsTriees = new ArrayList<>(livraisons);
        livraisonsTriees.sort((a, b) -> {
            double distA = distance(a.getAdresseEnlevement(), barycentre);
            double distB = distance(b.getAdresseEnlevement(), barycentre);
            return Double.compare(distB, distA);
        });

        // etape 4 : Attribution des livraisons
        for (Livraison l : livraisonsTriees) {
            // Calcul des distances de cette livraison à tous les centres
            List<Integer> ordreClusters = new ArrayList<>();
            for (int i = 0; i < k; i++) ordreClusters.add(i);

            ordreClusters.sort(Comparator.comparingDouble(i ->
                    distance(l.getAdresseEnlevement(), centresLivraisons.get(i).getAdresseEnlevement()))
            );

            // Ajout dans le premier cluster disponible selon l’ordre de proximite
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


    /**
     * Selectionne k centres initiaux espaces pour l'algorithme K-Means.
     *
     * @return Liste des livraisons choisies comme centres initiaux
     */
    private List<Livraison> choisirCentresEspaces() {
        List<Livraison> centres = new ArrayList<>();
        Random rand = new Random();

        // Premier centre choisi aleatoirement
        Livraison premier = livraisons.get(rand.nextInt(livraisons.size()));
        centres.add(premier);

        // Choisir les suivants comme les plus eloignes des precedents
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

    /**
     * Calcule le barycentre global des centres donnes.
     *
     * @param centres Liste des livraisons servant de centres
     * @return Tableau double[2] contenant la latitude et la longitude du barycentre
     */
    private double[] calculerBarycentre(List<Livraison> centres) {
        double sumLat = 0, sumLon = 0;
        for (Livraison l : centres) {
            sumLat += l.getAdresseEnlevement().getLatitude();
            sumLon += l.getAdresseEnlevement().getLongitude();
        }
        return new double[]{sumLat / centres.size(), sumLon / centres.size()};
    }

    /**
     * Calcule la distance euclidienne entre un noeud et un point donne.
     *
     * @param n     Le noeud de passage
     * @param point Tableau double[2] representant la latitude et la longitude du point
     * @return Distance euclidienne
     */
    private double distance(NoeudDePassage n, double[] point) {
        double dx = n.getLatitude() - point[0];
        double dy = n.getLongitude() - point[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calcule la distance euclidienne entre deux noeuds.
     *
     * @param n1 Premier noeud
     * @param n2 Deuxième noeud
     * @return Distance euclidienne
     */
    private double distance(NoeudDePassage n1, NoeudDePassage n2) {
        double dx = n1.getLatitude() - n2.getLatitude();
        double dy = n1.getLongitude() - n2.getLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }
}