package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.Chemin;

public class GrapheComplet implements Graphe {

    private int nbSommets;
    private double[][] cout; // coûts en double

    /**
     * Crée un graphe complet dont les arcs ont un coût initialisé à 0
     * @param nbSommets nombre de sommets
     */
    public GrapheComplet(int nbSommets) {
        this.nbSommets = nbSommets;
        cout = new double[nbSommets][nbSommets];
        for (int i = 0; i < nbSommets; i++) {
            for (int j = 0; j < nbSommets; j++) {
                cout[i][j] = (i == j) ? -1.0 : 0.0;
            }
        }
    }

    /**
     * Crée un graphe complet dont les arcs ont un coût initialisé à 0
     * @param nbSommets nombre de sommets
     */
    public GrapheComplet(int nbSommets, Chemin[][] matriceChemins) {
        this.nbSommets=nbSommets;
        cout=new double [nbSommets][nbSommets];
        for (int i = 0; i < nbSommets; i++) {
            for (int j = 0; j < nbSommets; j++) {
                if (i != j && matriceChemins[i][j] != null) {
                    cout[i][j]= matriceChemins[i][j].getLongueurTotal();

                }
            }
        }
    }

    @Override
    public int getNbSommets() {
        return nbSommets;
    }

    // Retourne le coût arrondi en int pour compatibilité interface Graphe
    @Override
    public double getCout(int i, int j) {
        if (i < 0 || i >= nbSommets || j < 0 || j >= nbSommets) return -1;
        return cout[i][j];
    }

    @Override
    public void setCout(int i, int j, double valeur) {
        if (i >= 0 && i < nbSommets && j >= 0 && j < nbSommets) {
            cout[i][j] = valeur;
        }
    }



    @Override
    public boolean estArc(int i, int j) {
        if (i < 0 || i >= nbSommets || j < 0 || j >= nbSommets) return false;
        return i != j;
    }
}