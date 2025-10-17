package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

public class CalculTournee {

    private final Carte ville;
    private final DemandeDeLivraison demande;
    private final double vitesse; // m/s
    double longueurTotale = 0;
    double dureeTotale;

    public CalculTournee(Carte ville, DemandeDeLivraison demande, double vitesse) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
    }

    public Tournee calculerTournee() throws Exception {
        List<NoeudDePassage> noeuds = demande.getNoeudsDePassage();
        List<Livraison> livraisons = demande.getLivraisons();

        //  FloydWarshall
        FloydWarshall floyd = new FloydWarshall(ville);
        floyd.calculerMatrice(noeuds);
        Chemin[][] matriceChemins = floyd.getMatriceChemins();

        // Graphe complet
        GrapheComplet g = new GrapheComplet(noeuds.size());
        for (int i = 0; i < noeuds.size(); i++) {
            for (int j = 0; j < noeuds.size(); j++) {
                if (i != j && matriceChemins[i][j] != null) {
                    g.setCout(i, j, matriceChemins[i][j].getLongueurTotal());
                }
            }
        }

        // 3️⃣ TSP avec précédences
        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0); // départ depuis l'entrepôt
        List<Integer> solution = tsp.getSolution(0);

        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP n'a pas produit de solution !");
        }

        // Construire la liste des chemins (aller + retour)
        List<Chemin> cheminsTournee = new ArrayList<>();


        // Chemins aller
        for (int k = 0; k < solution.size() - 1; k++) {
            int i = solution.get(k);
            int j = solution.get(k + 1);
            Chemin c = matriceChemins[i][j];
            if (c != null) {
                cheminsTournee.add(c);
                longueurTotale += c.getLongueurTotal();
            }
        }

        //Ajouter le retour à l’entrepôt
        int dernierIndex = solution.get(solution.size() - 1);
        Chemin retour = matriceChemins[dernierIndex][0]; // 0 = entrepôt
        if (retour != null) {
            cheminsTournee.add(retour);
            longueurTotale += retour.getLongueurTotal();
        }

        // Créer la tournée finale
        dureeTotale = longueurTotale / vitesse;

        for (NoeudDePassage noeud : demande.getNoeudsDePassage()) {
            dureeTotale += noeud.getDuree();
        }

        return new Tournee(cheminsTournee, dureeTotale);

    }

    public double getLongueurTotale() {
        return this.longueurTotale;
    }

    public double getDureeTotale() {
        return this.dureeTotale;
    }
}
