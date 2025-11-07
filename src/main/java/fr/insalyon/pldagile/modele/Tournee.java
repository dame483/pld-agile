package fr.insalyon.pldagile.modele;

import java.util.List;
import java.util.Objects;

/**
 * Représente la tournée d'un livreur, composée d'une liste de chemins entre des nœuds de passage.
 * Contient également des informations sur la durée totale et la longueur totale de la tournée.
 */
public class Tournee {
    /** Le livreur associé à cette tournée. */
    private Livreur livreur;

    /** Liste des chemins formant la tournée. */
    private List<Chemin> chemins;

    /** Durée totale de la tournée (en minutes ou secondes selon convention). */
    private double dureeTotale;

    /** Longueur totale de la tournée (en unités métriques, par exemple mètres). */
    private double longueurTotale;

    /** Constructeur par défaut. */
    public Tournee() {}

    /**
     * Constructeur avec initialisation des chemins, durée totale et longueur totale.
     * @param chemins la liste des chemins
     * @param dureeTotale durée totale de la tournée
     * @param longueurTotale longueur totale de la tournée
     */
    public Tournee(List<Chemin> chemins, double dureeTotale, double longueurTotale) {
        this.chemins = chemins;
        this.dureeTotale = dureeTotale;
        this.longueurTotale = longueurTotale;
    }

    /**
     * Constructeur avec initialisation des chemins, durée totale et longueur totale.
     * @param chemins la liste des chemins
     * @param dureeTotale durée totale de la tournée
     * @param longueurTotale longueur totale de la tournée
     */
    public Tournee(Livreur livreur, List<Chemin> chemins, double dureeTotale, double longueurTotale) {
        this.livreur = livreur;
        this.chemins = chemins;
        this.dureeTotale = dureeTotale;
        this.longueurTotale = longueurTotale;
    }

    /** Getters et setters */

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

    /**
     * Retourne le nœud de passage correspondant à l'identifiant donné.
     * Cherche dans tous les chemins de la tournée.
     * @param idNoeud identifiant du nœud
     * @return le NoeudDePassage correspondant, ou null si inexistant
     */
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

    /** Deux tournées sont égales si elles ont les mêmes chemins, durée et livreur. */
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

    /** Représentation textuelle de la tournée, incluant le livreur, la durée et les chemins. */
    @Override
    public String toString() {
        return String.format("Tournee{livreur=%s, dureeTotale=%.2f, chemins=%s}", livreur, dureeTotale, chemins);
    }


}
