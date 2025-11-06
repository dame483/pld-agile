package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente un chemin constitué d'une liste de tronçons entre un noeud de passage de départ
 * et un noeud de passage d'arrivée, avec une longueur totale.
 */
public class Chemin {

    /**
     * Liste des tronçons constituant le chemin.
     */
    private List<Troncon> troncons = new ArrayList();

    /**
     * Longueur totale du chemin en unités cohérentes avec les tronçons.
     */
    private double longueurTotal;

    /**
     * Noeud de passage de départ du chemin.
     */
    private NoeudDePassage NoeudDePassageDepart;

    /**
     * Noeud de passage d'arrivée du chemin.
     */
    private NoeudDePassage NoeudDePassageArrivee;

    /**
     * Constructeur par défaut. Initialise un chemin vide.
     */
    public Chemin(){}

    /**
     * Constructeur complet.
     *
     * @param troncons Liste des tronçons composant le chemin.
     * @param longueurTotal Longueur totale du chemin.
     * @param noeudDePassageDepart Noeud de départ.
     * @param noeudDePassageArrivee Noeud d'arrivée.
     */
    public Chemin(List<Troncon> troncons, double longueurTotal, NoeudDePassage noeudDePassageDepart, NoeudDePassage noeudDePassageArrivee) {
        this.troncons = troncons;
        this.longueurTotal = longueurTotal;
        this.NoeudDePassageDepart = noeudDePassageDepart;
        this.NoeudDePassageArrivee = noeudDePassageArrivee;
    }
    /**
     * @return La liste des tronçons du chemin.
     */
    public List<Troncon> getTroncons() {
        return this.troncons;
    }

    /**
     * @return La longueur totale du chemin.
     */
    public double getLongueurTotal() {
        return this.longueurTotal;
    }

    /**
     * @return Le noeud de passage de départ.
     */
    public NoeudDePassage getNoeudDePassageDepart() {
        return this.NoeudDePassageDepart;
    }

    /**
     * @return Le noeud de passage d'arrivée.
     */
    public NoeudDePassage getNoeudDePassageArrivee() {
        return this.NoeudDePassageArrivee;
    }

    /**
     * Définit la liste des tronçons du chemin.
     *
     * @param troncons Liste de tronçons.
     */
    public void setTroncons(List<Troncon> troncons) {
        this.troncons = troncons;
    }

    /**
     * Définit la longueur totale du chemin.
     *
     * @param longueurTotal Longueur totale.
     */
    public void setLongueurTotal(double longueurTotal) {
        this.longueurTotal = longueurTotal;
    }

    /**
     * Définit le noeud de passage de départ.
     *
     * @param noeudDePassageDepart Noeud de départ.
     */
    public void setNoeudDePassageDepart(NoeudDePassage noeudDePassageDepart) {
        this.NoeudDePassageDepart = noeudDePassageDepart;
    }

    /**
     * Définit le noeud de passage d'arrivée.
     *
     * @param noeudDePassageArrivee Noeud d'arrivée.
     */
    public void setNoeudDePassageArrivee(NoeudDePassage noeudDePassageArrivee) {
        this.NoeudDePassageArrivee = noeudDePassageArrivee;
    }

    /**
     * Crée une copie profonde du chemin.
     *
     * @return Une nouvelle instance de Chemin identique mais indépendante.
     */
    public Chemin copieProfonde() {
        List<Troncon> tronconsCopie = new ArrayList<>(this.troncons); // si Troncon est immutable
        NoeudDePassage departCopie = this.NoeudDePassageDepart.copieProfonde();
        NoeudDePassage arriveeCopie = this.NoeudDePassageArrivee.copieProfonde();
        return new Chemin(tronconsCopie, this.longueurTotal, departCopie, arriveeCopie);
    }

    /**
     * Représentation textuelle du chemin.
     *
     * @return Chaîne décrivant le chemin, sa longueur et ses tronçons.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chemin{");
        sb.append("depart=").append(NoeudDePassageDepart.getId());
        sb.append(", arrivee=").append(NoeudDePassageArrivee.getId());
        sb.append(", longueurTotale=").append(String.format("%.2f", longueurTotal));
        sb.append(", troncons=[");

        for (int i = 0; i < troncons.size(); i++) {
            Troncon t = troncons.get(i);
            sb.append(t.getNomRue()); // ou t.getNomRue() si disponible
            if (i < troncons.size() - 1) sb.append(" -> ");
        }

        sb.append("]}");
        return sb.toString();
    }

    /**
     * Vérifie l'égalité entre deux chemins.
     *
     * @param o Objet à comparer.
     * @return true si les chemins sont identiques en tronçons, longueur et noeuds de départ/arrivée.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chemin chemin)) return false;
        return Double.compare(longueurTotal, chemin.longueurTotal) == 0
                && Objects.equals(troncons, chemin.troncons)
                && Objects.equals(NoeudDePassageDepart, chemin.NoeudDePassageDepart)
                && Objects.equals(NoeudDePassageArrivee, chemin.NoeudDePassageArrivee);
    }

    /**
     * Hash code basé sur les tronçons, la longueur totale et les noeuds de départ/arrivée.
     *
     * @return Valeur de hachage.
     */
    @Override
    public int hashCode() {
        return Objects.hash(troncons, longueurTotal, NoeudDePassageDepart, NoeudDePassageArrivee);
    }
}