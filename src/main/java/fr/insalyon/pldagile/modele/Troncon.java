package fr.insalyon.pldagile.modele;

import java.util.Objects;

/**
 * Représente un tronçon entre deux nœuds d'un graphe routier.
 * Chaque tronçon possède un identifiant de nœud d'origine et de destination,
 * une longueur (en mètres ou unité métrique), et un nom de rue.
 */
public class Troncon {

    /** Identifiant du nœud d'origine du tronçon. */
    private long idOrigine;

    /** Identifiant du nœud de destination du tronçon. */
    private long idDestination;

    /** Longueur du tronçon. */
    private double longueur;

    /** Nom de la rue correspondant au tronçon. */
    private String nomRue;

    /** Constructeur par défaut. */
    public Troncon() {}

    /**
     * Constructeur avec initialisation complète.
     * @param idOrigine identifiant du nœud d'origine
     * @param idDestination identifiant du nœud de destination
     * @param longueur longueur du tronçon
     * @param nomRue nom de la rue
     */
    public Troncon(long idOrigine, long idDestination, double longueur, String nomRue) {
        this.idOrigine = idOrigine;
        this.idDestination = idDestination;
        this.longueur = longueur;
        this.nomRue = nomRue;
    }

    /** @return l'identifiant du nœud d'origine */
    public long getIdOrigine() {
        return this.idOrigine;
    }

    /** @return l'identifiant du nœud de destination */
    public long getIdDestination() {
        return this.idDestination;
    }

    /** @return le nom de la rue du tronçon */
    public String getNomRue() {
        return this.nomRue;
    }

    /** @return la longueur du tronçon */
    public double getLongueur() {
        return this.longueur;
    }

    /** Représentation textuelle du tronçon. */
    @Override
    public String toString() {
        return String.format("Troncon{idOrigine=%d, idDestination=%d, longueur=%.2f, nomRue='%s'}",
                idOrigine, idDestination, longueur, nomRue);
    }

    /** Deux tronçons sont égaux si tous leurs attributs sont identiques. */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Troncon troncon)) return false;
        return idOrigine == troncon.idOrigine && idDestination == troncon.idDestination && Double.compare(longueur, troncon.longueur) == 0 && Objects.equals(nomRue, troncon.nomRue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOrigine, idDestination, longueur, nomRue);
    }
}