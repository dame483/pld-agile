package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;

public class Tournee {
    private List<Chemin> chemins = new ArrayList();
    private double dureeTotale;
    private double longueuerTotale;
    // longueur totale + liste livraisons +getters

    public Tournee(List<Chemin> chemins, double dureeTotale) {
        this.chemins = chemins;
        this.dureeTotale=dureeTotale;
    }

    public List<Chemin> getChemins() {
        return this.chemins;
    }

    public double getDureeTotale() {
        return this.dureeTotale;
    }

    public double getLongueuerTotale() {return this.longueuerTotale;}

    public void setChemins(List<Chemin> chemins) {
        this.chemins = chemins;
    }

    public void setDureeTotale(double dureeTotale) {
        this.dureeTotale = dureeTotale;
    }

    public void setLongueurTotale(double longueurTotale) {
        this.longueuerTotale = longueurTotale;
    }

    public void ajouterChemin(Chemin c, double vitesse) {
        this.chemins.add(c);
        double dureeTrajet = c.getLongueurTotal() / vitesse;
        double dureeService = c.getNoeudDePassageArrivee().getDuree();
        this.dureeTotale += dureeTrajet + dureeService;
        this.longueuerTotale= c.getLongueurTotal();
    }


}