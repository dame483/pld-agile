package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.List;
/**
 * Représente une demande de livraison avec un entrepôt et une liste de livraisons.
 */
public class DemandeDeLivraison {

    /**
     * Le noeud de passage représentant l'entrepôt.
     */
    private NoeudDePassage entrepot;

    /**
     * Liste des livraisons associées à cette demande.
     */
    private List<Livraison> livraisons;

    /**
     * Constructeur par défaut.
     */
    public DemandeDeLivraison() {}

    /**
     * Constructeur complet.
     *
     * @param entrepot Le noeud représentant l'entrepôt.
     * @param livraisons Liste des livraisons.
     */
    public DemandeDeLivraison(NoeudDePassage entrepot, List<Livraison> livraisons) {
        this.entrepot = entrepot;
        this.livraisons = livraisons;
    }

    /**
     * @return Le noeud de passage correspondant à l'entrepôt.
     */
    public NoeudDePassage getEntrepot() {
        return this.entrepot;
    }

    /**
     * @return La liste des livraisons.
     */
    public List<Livraison> getLivraisons() {
        return this.livraisons;
    }

    /**
     * Définit le noeud de passage correspondant à l'entrepôt.
     *
     * @param entrepot Le noeud de passage de l'entrepôt.
     */
    public void setEntrepot(NoeudDePassage entrepot) {
        this.entrepot = entrepot;
    }

    /**
     * Définit la liste des livraisons.
     *
     * @param livraisons La nouvelle liste de livraisons.
     */
    public void setLivraisons(List<Livraison> livraisons) {
        this.livraisons = livraisons;
    }

    /**
     * @return La liste de tous les noeuds de passage impliqués dans cette demande,
     * y compris l'entrepôt et les adresses de chaque livraison (retrait et livraison).
     */
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

    /**
     * Représentation textuelle de la demande de livraison.
     *
     * @return Chaîne représentant l'entrepôt et la liste des livraisons.
     */
    public String toString() {
        String var10000 = String.valueOf(this.entrepot);
        return "DemandeDeLivraison{entrepot=" + var10000 + ", livraisons=" + String.valueOf(this.livraisons) + "}";
    }
}