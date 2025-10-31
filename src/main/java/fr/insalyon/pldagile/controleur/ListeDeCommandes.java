package fr.insalyon.pldagile.controleur;

import java.util.Stack;

public class ListeDeCommandes {
    private Stack<Commande> historique = new Stack<>();
    private Stack<Commande> pileRefaire = new Stack<>();

    public void executerCommande(Commande commande) {
        commande.executer();
        historique.push(commande);
        pileRefaire.clear();
    }

    public void annuler() {
        if (!historique.isEmpty()) {
            Commande commande = historique.pop();
            commande.annuler();
            pileRefaire.push(commande);
        }
    }

    public void restaurer() {
        if (!pileRefaire.isEmpty()) {
            Commande commande = pileRefaire.pop();
            commande.executer();
            historique.push(commande);
        }
    }
}
