package fr.insalyon.pldagile.modele;

public class Livraison {
    private NoeudDePassage noeudEnlevement;
    private NoeudDePassage noeudLivraison;

    public Livraison() {}

    public Livraison(NoeudDePassage adresseEnlevement, NoeudDePassage adresseLivraison) {
        this.noeudEnlevement = adresseEnlevement;
        this.noeudLivraison = adresseLivraison;
    }

    public Livraison() {
    }

    public NoeudDePassage getAdresseEnlevement() {
        return this.noeudEnlevement;
    }

    public NoeudDePassage getAdresseLivraison() {
        return this.noeudLivraison;
    }

    public void setAdresseEnlevement(NoeudDePassage adresseEnlevement) {
        this.noeudEnlevement = adresseEnlevement;
    }

    public void setAdresseLivraison(NoeudDePassage adresseLivraison) {
        this.noeudLivraison = adresseLivraison;
    }

    public String toString() {
        String var10000 = String.valueOf(this.noeudEnlevement);
        return "Livraison {adresseEnlevement=" + var10000 + ", adresseLivraison=" + String.valueOf(this.noeudLivraison) + "}";
    }
}