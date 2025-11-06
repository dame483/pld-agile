package fr.insalyon.pldagile.modele;

import java.time.LocalTime;

/**
 * Représente un nœud spécifique dans une tournée de livraison, avec un type (entrepôt, pickup ou delivery),
 * une durée associée, et des horaires d'arrivée et de départ.
 */
public class NoeudDePassage extends Noeud {

    /** Type du nœud (PICKUP, DELIVERY, ENTREPOT). */
    private TypeNoeud type;

    /** Durée estimée pour effectuer l'action au nœud (en minutes ou secondes selon convention). */
    private double duree;

    /** Horaire d'arrivée prévu au nœud. */
    private LocalTime horaireArrivee;

    /** Horaire de départ prévu du nœud. */
    private LocalTime horaireDepart;

    /** Constructeur par défaut. */
    public NoeudDePassage() {}

    /**
     * Constructeur avec initialisation complète (id, coordonnées, type, durée, horaireArrivee).
     */
    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree, LocalTime horaireArrivee) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = horaireArrivee;
        this.horaireDepart = null;
    }

    /**
     * Constructeur minimal avec id, coordonnées et type.
     */
    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type) {
        super(id, latitude, longitude);
        this.type = type;
    }

    /**
     * Constructeur avec id, coordonnées, type et durée.
     */
    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = null;
        this.horaireDepart = null;
    }

    /**
     * Constructeur complet avec horaires d'arrivée et de départ.
     */
    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree, LocalTime horaireArrivee, LocalTime horaireDepart) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = horaireArrivee;
        this.horaireDepart = horaireDepart;
    }

    /** Getters et setters pour tous les champs. */
    public TypeNoeud getType() {
        return this.type;
    }

    public double getDuree() {
        return this.duree;
    }

    public LocalTime getHoraireArrivee() {
        return this.horaireArrivee;
    }

    public LocalTime getHoraireDepart() {
        return this.horaireDepart;
    }

    public void setType(TypeNoeud type) {
        this.type = type;
    }

    public void setDuree(double duree) {
        this.duree = duree;
    }

    public void setHoraireArrivee(LocalTime horaireArrivee) {
        this.horaireArrivee = horaireArrivee;
    }

    public void setHoraireDepart(LocalTime horaireDepart) {
        this.horaireDepart = horaireDepart;
    }

    /**
     * Crée une copie profonde du nœud de passage.
     * @return une nouvelle instance avec les mêmes valeurs.
     */
    public NoeudDePassage copieProfonde() {
        NoeudDePassage copie = new NoeudDePassage(
                this.getId(),
                this.getLatitude(),
                this.getLongitude(),
                this.getType(),
                this.getDuree()
        );
        copie.setHoraireDepart(this.getHoraireDepart());
        copie.setHoraireArrivee(this.getHoraireArrivee());
        return copie;
    }

    /**
     * Deux NoeudDePassage sont égaux si leurs identifiants sont identiques.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoeudDePassage)) return false;
        NoeudDePassage other = (NoeudDePassage) o;
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.getId());
    }

    /**
     * Retourne une représentation textuelle complète du nœud de passage.
     */
    @Override
    public String toString() {
        return "NoeudDePassage {id=" + getId()
                + ", latitude=" + getLatitude()
                + ", longitude=" + getLongitude()
                + ", type=" + type
                + ", duree=" + duree
                + ", horaireArrivee=" + horaireArrivee
                + ", horaireDepart=" + horaireDepart + "}";
    }

    /**
     * Type d'un nœud dans une tournée.
     */
    public static enum TypeNoeud {
        /** Nœud de prise en charge (pickup). */
        PICKUP,

        /** Nœud de livraison (delivery). */
        DELIVERY,

        /** Nœud représentant un entrepôt. */
        ENTREPOT;
    }
}
