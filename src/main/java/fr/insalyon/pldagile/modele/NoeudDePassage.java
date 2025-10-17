package fr.insalyon.pldagile.modele;

import java.time.LocalTime;

public class NoeudDePassage extends Noeud {
    private TypeNoeud type;
    private double duree;
    private LocalTime horaireArrivee;

    public NoeudDePassage(long id, double latitude, double longitude, TypeNoeud type, double duree, LocalTime horaireArrivee) {
        super(id, latitude, longitude);
        this.type = type;
        this.duree = duree;
        this.horaireArrivee = horaireArrivee;
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

    public void setType(TypeNoeud type) {
        this.type = type;
    }

    public void setDuree(double duree) {
        this.duree = duree;
    }

    public void setHoraireArrivee(LocalTime horaireArrivee) {
        this.horaireArrivee = horaireArrivee;
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


    public String toString() {
        long var10000 = this.getId();
        return "NoeudDePassage {id=" + var10000 + ", latitude=" + this.getLatitude() + ", longitude=" + this.getLongitude() + ", type=" + String.valueOf(this.type) + ", duree=" + this.duree + ", horaireArrivee=" + String.valueOf(this.horaireArrivee) + "}";
    }

    public static enum TypeNoeud {
        PICKUP,
        DELIVERY,
        ENTREPOT;
    }
}