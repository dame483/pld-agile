package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Représente une carte routière composée de noeuds et de tronçons.
 */
public class Carte {

    /** Ensemble des noeuds identifiés par leur ID. */
    private HashMap<Long, Noeud> noeuds = new HashMap<>();

    /** Liste des tronçons de la carte. */
    private List<Troncon> troncons = new ArrayList<>();

    /**
     * Ajoute un noeud à la carte.
     *
     * @param n Le noeud à ajouter.
     */
    public void AjouterNoeud(Noeud n) {
        this.noeuds.put(n.getId(), n);
    }

    /**
     * Ajoute un tronçon à la carte.
     *
     * @param t Le tronçon à ajouter.
     */
    public void AjouterTroncon(Troncon t) {
        this.troncons.add(t);
    }

    /**
     * Retourne l'ensemble des noeuds de la carte.
     *
     * @return La map des noeuds.
     */
    public HashMap<Long, Noeud> getNoeuds() {
        return this.noeuds;
    }

    /**
     * Retourne la liste des tronçons de la carte.
     *
     * @return La liste des tronçons.
     */
    public List<Troncon> getTroncons() {
        return this.troncons;
    }

    /**
     * Retourne un noeud à partir de son identifiant.
     *
     * @param id L'identifiant du noeud.
     * @return Le noeud correspondant, ou null si l'ID n'existe pas.
     */
    public Noeud getNoeudParId(long id) {
        return noeuds.get(id);
    }
}
