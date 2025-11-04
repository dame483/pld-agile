package fr.insalyon.pldagile.modele;

import java.util.Objects;

public class Troncon {
    private long idOrigine;
    private long idDestination;
    private double longueur;
    private String nomRue;

    public Troncon() {}

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

    @Override
    public String toString() {
        return String.format("Troncon{idOrigine=%d, idDestination=%d, longueur=%.2f, nomRue='%s'}",
                idOrigine, idDestination, longueur, nomRue);
    }


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