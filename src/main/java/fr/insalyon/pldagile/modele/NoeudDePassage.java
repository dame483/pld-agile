package fr.insalyon.pldagile.modele;

import java.time.LocalTime;

public class NoeudDePassage extends Noeud {
    private TypeNoeud type;
    private double duree;
    private LocalTime horaireArrivee;
    private LocalTime horaireDepart;

    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree, LocalTime horaireArrivee) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = horaireArrivee;
        this.horaireDepart = null;
    }

    // Constructeur pour l'entrepôt
    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree, LocalTime horaireArrivee, LocalTime horaireDepart) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = horaireArrivee;
        this.horaireDepart = horaireDepart;
    }

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

    public static enum TypeNoeud {
        PICKUP,
        DELIVERY,
        ENTREPOT;
    }
}
