package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;

public class Tournee {
    private List<Chemin> chemins = new ArrayList();
    private double dureeTotale;

    public Tournee(List<Chemin> chemins, double dureeTotale) {
        this.chemins = chemins;
        this.dureeTotale = dureeTotale;
    }

    public List<Chemin> getChemins() {
        return this.chemins;
    }

    public double getDureeTotale() {
        return this.dureeTotale;
    }

    public void setChemins(List<Chemin> chemins) {
        this.chemins = chemins;
    }

    public void setDureeTotale(double dureeTotale) {
        this.dureeTotale = dureeTotale;
    }

    public void ajouterChemin(Chemin c, double vitesse) {
        this.chemins.add(c);
        double dureeTrajet = c.getLongueurTotal() / vitesse;
        double dureeService = c.getNoeudDePassageArrivee().getDuree();
        this.dureeTotale += dureeTrajet + dureeService;
    }

    public double getDistanceTotale() {
        return chemins.stream()
                .mapToDouble(Chemin::getLongueurTotal)
                .sum();
    }

}