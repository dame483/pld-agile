package fr.insalyon.pldagile.modele;

public class Livraison {
    private NoeudDePassage adresseEnlevement;
    private NoeudDePassage adresseLivraison;

    public Livraison() {}

    public Livraison(NoeudDePassage adresseEnlevement, NoeudDePassage adresseLivraison) {
        this.adresseEnlevement = adresseEnlevement;
        this.adresseLivraison = adresseLivraison;
    }

    public NoeudDePassage getAdresseEnlevement() {
        return this.adresseEnlevement;
    }

    public NoeudDePassage getAdresseLivraison() {
        return this.adresseLivraison;
    }

    public void setAdresseEnlevement(NoeudDePassage adresseEnlevement) {
        this.adresseEnlevement = adresseEnlevement;
    }

    public void setAdresseLivraison(NoeudDePassage adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String toString() {
        String var10000 = String.valueOf(this.adresseEnlevement);
        return "Livraison {adresseEnlevement=" + var10000 + ", adresseLivraison=" + String.valueOf(this.adresseLivraison) + "}";
    }
}