package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;

public class DemandeDeLivraison {
    private NoeudDePassage entrepot;
    private List<Livraison> livraisons;

    public DemandeDeLivraison(NoeudDePassage entrepot, List<Livraison> livraisons) {
        this.entrepot = entrepot;
        this.livraisons = livraisons;
    }

    public NoeudDePassage getEntrepot() {
        return this.entrepot;
    }

    public List<Livraison> getLivraisons() {
        return this.livraisons;
    }

    public void setEntrepot(NoeudDePassage entrepot) {
        this.entrepot = entrepot;
    }

    public void setLivraisons(List<Livraison> livraisons) {
        this.livraisons = livraisons;
    }

    public List<NoeudDePassage> getNoeudsDePassage() {
        List<NoeudDePassage> noeuds = new ArrayList<>();
        noeuds.add(entrepot);
        for (Livraison l : livraisons) {
            if (!noeuds.contains(l.getAdresseEnlevement())) {
                noeuds.add(l.getAdresseEnlevement());
            }
            if (!noeuds.contains(l.getAdresseLivraison())) {
                noeuds.add(l.getAdresseLivraison());
            }
        }
        return noeuds;
    }

    public String toString() {
        String var10000 = String.valueOf(this.entrepot);
        return "DemandeDeLivraison{entrepot=" + var10000 + ", livraisons=" + String.valueOf(this.livraisons) + "}";
    }
}