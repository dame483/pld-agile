package fr.insalyon.pldagile.modele;

import java.util.List;
import java.util.Objects;

public class Tournee {
    private Livreur livreur; // <-- nouveau champ
    private List<Chemin> chemins;
    private double dureeTotale;
    private double longueurTotale;

    public Tournee() {}

    public Tournee(List<Chemin> chemins, double dureeTotale, double longueurTotale) {
        this.chemins = chemins;
        this.dureeTotale = dureeTotale;
        this.longueurTotale = longueurTotale;
    }

    public Tournee(Livreur livreur, List<Chemin> chemins, double dureeTotale, double longueurTotale) {
        this.livreur = livreur;
        this.chemins = chemins;
        this.dureeTotale = dureeTotale;
        this.longueurTotale = longueurTotale;
    }


    public Livreur getLivreur() {
        return livreur;
    }

    public void setLivreur(Livreur livreur) {
        this.livreur = livreur;
    }

    public List<Chemin> getChemins() {
        return chemins;
    }

    public double getDureeTotale() {
        return dureeTotale;
    }

    public double getLongueurTotale() {
        return longueurTotale;
    }

    public void setChemins(List<Chemin> chemins) {
        this.chemins = chemins;
    }

    public void setDureeTotale(double dureeTotale) {
        this.dureeTotale = dureeTotale;
    }

    public void setLongueurTotale(double longueurTotale) {
        this.longueurTotale = longueurTotale;
    }

    public NoeudDePassage getNoeudParId(long idNoeud) {
        for (Chemin c : chemins) {
            if (Objects.equals(c.getNoeudDePassageDepart().getId(), idNoeud)) {
                return c.getNoeudDePassageDepart();
            }
            if (Objects.equals(c.getNoeudDePassageArrivee().getId(), idNoeud)) {
                return c.getNoeudDePassageArrivee();
            }
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tournee tournee)) return false;
        return Double.compare(dureeTotale, tournee.dureeTotale) == 0
                && Objects.equals(chemins, tournee.chemins)
                && Objects.equals(livreur, tournee.livreur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(livreur, chemins, dureeTotale);
    }

    @Override
    public String toString() {
        return String.format("Tournee{livreur=%s, dureeTotale=%.2f, chemins=%s}", livreur, dureeTotale, chemins);
    }


}
