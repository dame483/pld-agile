package fr.insalyon.pldagile.algorithme;

/**
 * Interface représentant un graphe orienté pondéré.
 *
 * <p>Un graphe est défini par un ensemble de sommets et des arcs entre ces sommets.
 * Chaque arc peut avoir un coût associé, qui peut être mis à jour.</p>
 */
public interface Graphe {

	/**
	 * @return le nombre de sommets de <code>this</code>
	 */
	public abstract int getNbSommets();

	/**
	 * @param i 
	 * @param j 
	 * @return le cout de l'arc (i,j) si (i,j) est un arc ; -1 sinon
	 */
	public abstract double getCout(int i, int j);


    public abstract void setCout(int i, int j, double cout);

    /**
	 * @param i 
	 * @param j 
	 * @return true si <code>(i,j)</code> est un arc de <code>this</code>
	 */
	public abstract boolean estArc(int i, int j);


}