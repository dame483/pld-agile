package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;

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

    public String toString() {
        String var10000 = String.valueOf(this.troncons);
        return "Chemin{troncons=" + var10000 + ", longueurTotal=" + this.longueurTotal + ", NoeudDePassageDepart=" + String.valueOf(this.NoeudDePassageDepart) + ", NoeudDePassageArrivee=" + String.valueOf(this.NoeudDePassageArrivee) + "}";
    }
}