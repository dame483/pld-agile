package fr.insalyon.pldagile.modele;

/**
 * Représente un nœud géographique avec un identifiant unique et des coordonnées.
 */
public class Noeud {

    /** Identifiant unique du nœud. */
    private long id;

    /** Latitude du nœud. */
    private double latitude;

    /** Longitude du nœud. */
    private double longitude;

    /**
     * Constructeur par défaut.
     */
    public Noeud() {}

    /**
     * Constructeur avec initialisation de l'identifiant et des coordonnées.
     *
     * @param id L'identifiant unique du nœud.
     * @param latitude La latitude du nœud.
     * @param longitude La longitude du nœud.
     */
    public Noeud(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Retourne l'identifiant du nœud.
     *
     * @return L'identifiant unique.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Retourne la latitude du nœud.
     *
     * @return La latitude.
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Retourne la longitude du nœud.
     *
     * @return La longitude.
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Retourne une représentation textuelle du nœud.
     *
     * @return Une chaîne décrivant l'identifiant, la latitude et la longitude du nœud.
     */
    public String toString() {
        return "Noeud {id = " + this.id + ", latitude = " + this.latitude + ", longitude = " + this.longitude + "}";
    }
}