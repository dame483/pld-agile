package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.exception.TourneeNonConnexeException;
import fr.insalyon.pldagile.modele.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CalculTournees {

    private final Carte ville;
    private final DemandeDeLivraison demande;
    private final double vitesse; // m/s
    private final LocalTime heureDepart;
    private final int nombreLivreurs;
    private double longueurTotale = 0;
    private double dureeTotale = 0;

    public CalculTournees(Carte ville, DemandeDeLivraison demande, double vitesse, LocalTime heureDepart, int nombreLivreurs) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
        this.heureDepart = heureDepart;
        this.nombreLivreurs = nombreLivreurs;
    }

    /**
     * Calcule toutes les tournées pour tous les livreurs en utilisant K-Means + TSP
     */
    public List<Tournee> calculerTournees() throws TourneeNonConnexeException {

        List<Tournee> toutesLesTournees = new ArrayList<>();

        // Étape 1 : Clustering K-Means
        KMeans kmeans = new KMeans(demande.getLivraisons(), nombreLivreurs);
        List<List<Livraison>> clusters = kmeans.cluster();

        // Étape 2 : Calcul des tournées par cluster
        for (List<Livraison> clusterLivraisons : clusters) {
            if (clusterLivraisons == null || clusterLivraisons.isEmpty()) continue;

            DemandeDeLivraison demandeCluster = new DemandeDeLivraison(demande.getEntrepot(), clusterLivraisons);

            List<NoeudDePassage> noeuds = demandeCluster.getNoeudsDePassage();
            CalculChemins chemins = calculerPlusCourtsChemins(noeuds);
            Chemin[][] matriceChemins = chemins.getMatriceChemins();

            verifierConnexiteChemins(matriceChemins);

            GrapheComplet g = construireGrapheComplet(noeuds, matriceChemins);
            List<Integer> solution;

            // Étape 3 : Choisir l'algorithme selon le nombre de livraisons
            if (clusterLivraisons.size() <= 5) {
                solution = resoudreTSP(noeuds, clusterLivraisons, g);
            } else {
                solution = resoudreTSPHeuristique(noeuds, clusterLivraisons, g);
            }

            List<Chemin> cheminsTournee = new ArrayList<>();
            LocalTime heureFin = calculerCheminsEtHoraires(solution, noeuds, matriceChemins, cheminsTournee);
            ajouterRetourEntrepot(solution, noeuds, matriceChemins, cheminsTournee, heureFin);

            Tournee tournee = new Tournee(cheminsTournee, dureeTotale);
            toutesLesTournees.add(tournee);

            // Réinitialiser les compteurs pour le prochain cluster
            longueurTotale = 0;
            dureeTotale = 0;
        }

        return toutesLesTournees;
    }

    private CalculChemins calculerPlusCourtsChemins(List<NoeudDePassage> noeuds) {
        CalculChemins chemins = new CalculChemins(ville);
        chemins.calculerMatrice(noeuds);
        return chemins;
    }

    private GrapheComplet construireGrapheComplet(List<NoeudDePassage> noeuds, Chemin[][] matriceChemins) {
        return new GrapheComplet(noeuds.size(), matriceChemins);
    }

    private List<Integer> resoudreTSP(List<NoeudDePassage> noeuds, List<Livraison> livraisons, GrapheComplet g) {
        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0);
        List<Integer> solution = tsp.getSolution(0);
        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP n’a produit aucune solution !");
        }
        return solution;
    }

    private List<Integer> resoudreTSPHeuristique(List<NoeudDePassage> noeuds, List<Livraison> livraisons, GrapheComplet g) {
        TSPHeuristique tsp = new TSPHeuristique(noeuds, livraisons, g);
        List<Integer> solution = tsp.calculerTourneeHeuristique(0);
        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP n’a produit aucune solution !");
        }
        return solution;
    }

    private LocalTime calculerCheminsEtHoraires(List<Integer> solution, List<NoeudDePassage> noeuds, Chemin[][] matriceChemins, List<Chemin> cheminsTournee) {
        LocalTime heureCourante = heureDepart;
        NoeudDePassage entrepot = noeuds.get(solution.get(0));
        entrepot.setHoraireDepart(heureDepart);

        for (int k = 0; k < solution.size() - 1; k++) {
            int idxDepart = solution.get(k);
            int idxArrivee = solution.get(k + 1);
            Chemin chemin = matriceChemins[idxDepart][idxArrivee];
            if (chemin == null) continue;

            NoeudDePassage depart = chemin.getNoeudDePassageDepart();
            NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();

            depart.setHoraireDepart(heureCourante);

            double dureeTrajetSec = chemin.getLongueurTotal() / vitesse;
            LocalTime heureArrivee = heureCourante.plusSeconds(Math.round(dureeTrajetSec));
            arrivee.setHoraireArrivee(heureArrivee);

            heureCourante = heureArrivee.plusSeconds(Math.round(arrivee.getDuree()));
            arrivee.setHoraireDepart(heureCourante);

            longueurTotale += chemin.getLongueurTotal();
            dureeTotale += dureeTrajetSec + arrivee.getDuree();

            cheminsTournee.add(chemin);
        }

        return heureCourante;
    }

    private void ajouterRetourEntrepot(List<Integer> solution, List<NoeudDePassage> noeuds, Chemin[][] matriceChemins, List<Chemin> cheminsTournee, LocalTime heureCourante) {
        int dernierIdx = solution.get(solution.size() - 1);
        Chemin retour = matriceChemins[dernierIdx][solution.get(0)];
        if (retour == null) return;

        NoeudDePassage depart = retour.getNoeudDePassageDepart();
        NoeudDePassage arrivee = retour.getNoeudDePassageArrivee();

        depart.setHoraireDepart(heureCourante);

        double dureeTrajetRetour = retour.getLongueurTotal() / vitesse;
        LocalTime heureArriveeFinale = heureCourante.plusSeconds(Math.round(dureeTrajetRetour));

        arrivee.setHoraireArrivee(heureArriveeFinale);
        noeuds.get(0).setHoraireArrivee(heureArriveeFinale);

        longueurTotale += retour.getLongueurTotal();
        dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);

        cheminsTournee.add(retour);
    }

    private void verifierConnexiteChemins(Chemin[][] matriceChemins) throws TourneeNonConnexeException {
        int n = matriceChemins.length;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if (matriceChemins[i][j] == null || matriceChemins[i][j].getLongueurTotal() == Double.POSITIVE_INFINITY) {
                    throw new TourneeNonConnexeException(
                            "La tournée ne peut pas être calculée : le graphe des chemins n'est pas connexe entre "
                                    + i + " et " + j
                    );
                }
            }
        }
    }

    public double getLongueurTotale() {
        return longueurTotale;
    }

    public double getDureeTotale() {
        return dureeTotale;
    }
}
