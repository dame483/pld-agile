package fr.insalyon.pldagile.controleur;

import java.util.LinkedList;

/**
 * Gère l'historique des commandes pour permettre l'exécution, l'annulation et la restauration.
 */
public class ListeDeCommandes {

    private LinkedList<Commande> commandes = new LinkedList<>();
    private int indexActuel = -1;

    /**
     * Exécute une commande et l'ajoute à l'historique.
     * La pile de restauration est vidée après l'exécution.
     *
     * @param commande La commande à exécuter.
     */
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

    /**
     * Annule la dernière commande exécutée.
     * La commande annulée est ajoutée à la pile de restauration.
     */
    public void annuler() {
        if (indexActuel >= 0) {
            commandes.get(indexActuel).annuler();
            indexActuel--;
        }
    }

    /**
     * Restaure la dernière commande annulée.
     * La commande restaurée est ajoutée à l'historique.
     */
    public void restaurer() {
        if (indexActuel < commandes.size() - 1) {
            indexActuel++;
            commandes.get(indexActuel).executer();
        }
    }
}
