package fr.insalyon.pldagile.modele;

public class Troncon {
    private long idOrigine;
    private long idDestination;
    private double longueur;
    private String nomRue;

    public Troncon(long idOrigine, long idDestination, double longueur, String nomRue) {
        this.idOrigine = idOrigine;
        this.idDestination = idDestination;
        this.longueur = longueur;
        this.nomRue = nomRue;
    }

    public long getIdOrigine() {
        return this.idOrigine;
    }

    public long getIdDestination() {
        return this.idDestination;
    }

    public String getNomRue() {
        return this.nomRue;
    }

    public double getLongueur() {
        return this.longueur;
    }

    public String toString() {
        return "Tronçon {Rue : " + this.nomRue + ", Origine : " + this.idOrigine + " -> Destination : " + this.idDestination + ", Longueur : " + this.longueur + " m}";
    }
}