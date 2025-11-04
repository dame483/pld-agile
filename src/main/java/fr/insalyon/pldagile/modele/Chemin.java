package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chemin {
    private List<Troncon> troncons = new ArrayList();
    private double longueurTotal;
    private NoeudDePassage NoeudDePassageDepart;
    private NoeudDePassage NoeudDePassageArrivee;

    public Chemin(){}

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


    public Chemin copieProfonde() {
        List<Troncon> tronconsCopie = new ArrayList<>(this.troncons); // si Troncon est immutable
        NoeudDePassage departCopie = this.NoeudDePassageDepart.copieProfonde();
        NoeudDePassage arriveeCopie = this.NoeudDePassageArrivee.copieProfonde();
        return new Chemin(tronconsCopie, this.longueurTotal, departCopie, arriveeCopie);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chemin{");
        sb.append("depart=").append(NoeudDePassageDepart.getId());
        sb.append(", arrivee=").append(NoeudDePassageArrivee.getId());
        sb.append(", longueurTotale=").append(String.format("%.2f", longueurTotal));
        sb.append(", troncons=[");

        for (int i = 0; i < troncons.size(); i++) {
            Troncon t = troncons.get(i);
            sb.append(t.getNomRue()); // ou t.getNomRue() si disponible
            if (i < troncons.size() - 1) sb.append(" -> ");
        }

        sb.append("]}");
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chemin chemin)) return false;
        return Double.compare(longueurTotal, chemin.longueurTotal) == 0
                && Objects.equals(troncons, chemin.troncons)
                && Objects.equals(NoeudDePassageDepart, chemin.NoeudDePassageDepart)
                && Objects.equals(NoeudDePassageArrivee, chemin.NoeudDePassageArrivee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(troncons, longueurTotal, NoeudDePassageDepart, NoeudDePassageArrivee);
    }
}