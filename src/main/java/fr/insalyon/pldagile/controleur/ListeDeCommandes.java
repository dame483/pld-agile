package fr.insalyon.pldagile.controleur;

import java.util.LinkedList;

public class ListeDeCommandes {

    private LinkedList<Commande> commandes = new LinkedList<>();
    private int indexActuel = -1;

    public void executerCommande(Commande commande) {
        commande.executer();
        indexActuel++;

        // Ajoute à la position actuelle et garde l'historique complet
        if (indexActuel < commandes.size()) {
            commandes.add(indexActuel, commande);
        } else {
            commandes.add(commande);
        }
    }

    public void annuler() {
        if (indexActuel >= 0) {
            commandes.get(indexActuel).annuler();
            indexActuel--;
        }
    }

    public void restaurer() {
        if (indexActuel < commandes.size() - 1) {
            indexActuel++;
            commandes.get(indexActuel).executer();
        }
    }
}
