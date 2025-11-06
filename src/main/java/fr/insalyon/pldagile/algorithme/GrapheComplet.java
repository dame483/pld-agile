package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.Chemin;

/**
 * Implémentation d'un graphe complet pondéré.
 *
 * <p>Dans un graphe complet, chaque paire de sommets distincts est reliée par un arc.
 * Les coûts des arcs peuvent être initialisés à partir d'une matrice de chemins ou à zéro.</p>
 */
public class GrapheComplet implements Graphe {

    /** Nombre de sommets du graphe */
    private int nbSommets;

    /** Matrice des coûts des arcs entre les sommets */
    private double[][] cout;

    /**
     * Crée un graphe complet avec un nombre donné de sommets.
     * Les arcs ont un coût de 0.0, sauf pour les arcs de boucle (i,i) qui valent -1.
     *
     * @param nbSommets le nombre de sommets du graphe
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
     * Crée un graphe complet avec des coûts initialisés à partir d'une matrice de chemins.
     * Si un chemin entre i et j est null, le coût reste à 0.0.
     *
     * @param nbSommets le nombre de sommets du graphe
     * @param matriceChemins matrice de chemins contenant les longueurs des arcs
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbSommets() {
        return nbSommets;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean estArc(int i, int j) {
        if (i < 0 || i >= nbSommets || j < 0 || j >= nbSommets) return false;
        return i != j;
    }
}
