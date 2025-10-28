package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CalculateurTournees {

    private final Carte ville;
    private final DemandeDeLivraison demande;
    private final double vitesse;
    private final LocalTime heureDepart;
    private final int nombreLivreurs;

    public CalculateurTournees(Carte ville, DemandeDeLivraison demande, double vitesse, LocalTime heureDepart, int nombreLivreurs) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
        this.heureDepart = heureDepart;
        this.nombreLivreurs = nombreLivreurs;
    }

    public List<Tournee> calculerTournees() throws Exception {
        List<Tournee> toutesLesTournees = new ArrayList<>();

        // Étape 1 : Clustering K-Means
        KMeans kmeans = new KMeans(demande.getLivraisons(), nombreLivreurs);
        List<List<Livraison>> clusters = kmeans.cluster();

        for (int i = 0; i < clusters.size(); i++) {
            List<Livraison> clusterLivraisons = clusters.get(i);

            // Créer une demande spécifique pour ce cluster
            DemandeDeLivraison demandeCluster = new DemandeDeLivraison(demande.getEntrepot(), clusterLivraisons);

            Tournee tournee;

            // Étape 2 : Choisir l'algorithme selon le nombre de livraisons
            if (clusterLivraisons.size() <= 5) {
                // TSPAvecPrecedence
                CalculTourneeAvecPrecedence calc = new CalculTourneeAvecPrecedence(ville, demandeCluster, vitesse, heureDepart);
                tournee = calc.calculerTournee();
            } else {
                // TSPHeuristique
                CalculTourneeHeuristique calc = new CalculTourneeHeuristique(ville, demandeCluster, vitesse, heureDepart);
                tournee = calc.calculerTournee();
            }

            toutesLesTournees.add(tournee);
        }

        return toutesLesTournees;
    }
}
