package fr.insalyon.pldagile.modele;

/**
 * Représente une livraison avec un noeud d'enlèvement et un noeud de livraison.
 */
public class Livraison {

    /** Noeud de passage représentant l'adresse d'enlèvement. */
    private NoeudDePassage noeudEnlevement;

    /** Noeud de passage représentant l'adresse de livraison. */
    private NoeudDePassage noeudLivraison;

    /**
     * Constructeur par défaut.
     */
    public Livraison() {}

    /**
     * Constructeur avec les noeuds d'enlèvement et de livraison.
     *
     * @param adresseEnlevement Le noeud de passage d'enlèvement.
     * @param adresseLivraison Le noeud de passage de livraison.
     */
    public Livraison(NoeudDePassage adresseEnlevement, NoeudDePassage adresseLivraison) {
        this.noeudEnlevement = adresseEnlevement;
        this.noeudLivraison = adresseLivraison;
    }

    /**
     * Retourne le noeud d'enlèvement.
     *
     * @return Le {@link NoeudDePassage} d'enlèvement.
     */
    public NoeudDePassage getAdresseEnlevement() {
        return this.noeudEnlevement;
    }

    /**
     * Retourne le noeud de livraison.
     *
     * @return Le {@link NoeudDePassage} de livraison.
     */
    public NoeudDePassage getAdresseLivraison() {
        return this.noeudLivraison;
    }

    /**
     * Définit le noeud d'enlèvement.
     *
     * @param adresseEnlevement Le {@link NoeudDePassage} à définir comme noeud d'enlèvement.
     */
    public void setAdresseEnlevement(NoeudDePassage adresseEnlevement) {
        this.noeudEnlevement = adresseEnlevement;
    }


    /**
     * Définit le noeud de livraison.
     *
     * @param adresseLivraison Le {@link NoeudDePassage} à définir comme noeud de livraison.
     */
    public void setAdresseLivraison(NoeudDePassage adresseLivraison) {
        this.noeudLivraison = adresseLivraison;
    }

    /**
     * Retourne une représentation textuelle de la livraison.
     *
     * @return Une chaîne décrivant l'adresse d'enlèvement et l'adresse de livraison.
     */
    public String toString() {
        String var10000 = String.valueOf(this.noeudEnlevement);
        return "Livraison {adresseEnlevement=" + var10000 + ", adresseLivraison=" + String.valueOf(this.noeudLivraison) + "}";
    }
}