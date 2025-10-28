package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chemin {
    private List<Troncon> troncons = new ArrayList();
    private double longueurTotal;
    private NoeudDePassage NoeudDePassageDepart;
    private NoeudDePassage NoeudDePassageArrivee;

    public Chemin(List<Troncon> troncons, double longueurTotal, NoeudDePassage noeudDePassageDepart, NoeudDePassage noeudDePassageArrivee) {
        this.troncons = troncons;
        this.longueurTotal = longueurTotal;
        this.NoeudDePassageDepart = noeudDePassageDepart;
        this.NoeudDePassageArrivee = noeudDePassageArrivee;
    }

    public List<Troncon> getTroncons() {
        return this.troncons;
    }

    public double getLongueurTotal() {
        return this.longueurTotal;
    }

    public NoeudDePassage getNoeudDePassageDepart() {
        return this.NoeudDePassageDepart;
    }

    public NoeudDePassage getNoeudDePassageArrivee() {
        return this.NoeudDePassageArrivee;
    }

    public void setTroncons(List<Troncon> troncons) {
        this.troncons = troncons;
    }

    public void setLongueurTotal(double longueurTotal) {
        this.longueurTotal = longueurTotal;
    }

    public void setNoeudDePassageDepart(NoeudDePassage noeudDePassageDepart) {
        this.NoeudDePassageDepart = noeudDePassageDepart;
    }

    public void setNoeudDePassageArrivee(NoeudDePassage noeudDePassageArrivee) {
        this.NoeudDePassageArrivee = noeudDePassageArrivee;
    }

    @Override
    public String toString() {
        return String.format("Chemin{longueurTotale=%.2f, troncons=%s}", longueurTotal, troncons);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chemin chemin)) return false;
        return Double.compare(longueurTotal, chemin.longueurTotal) == 0 && Objects.equals(troncons, chemin.troncons) && Objects.equals(NoeudDePassageDepart, chemin.NoeudDePassageDepart) && Objects.equals(NoeudDePassageArrivee, chemin.NoeudDePassageArrivee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(troncons, longueurTotal, NoeudDePassageDepart, NoeudDePassageArrivee);
    }
}