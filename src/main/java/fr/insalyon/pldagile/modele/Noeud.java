package fr.insalyon.pldagile.modele;

public class Noeud {
    private long id;
    private double latitude;
    private double longitude;

    public Noeud() {}

    public Noeud(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return this.id;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String toString() {
        return "Noeud {id = " + this.id + ", latitude = " + this.latitude + ", longitude = " + this.longitude + "}";
    }
}