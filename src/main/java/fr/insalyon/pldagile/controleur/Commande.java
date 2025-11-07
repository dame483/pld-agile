package fr.insalyon.pldagile.controleur;

/**
 * Interface représentant le patron de conception Commande (Command Pattern).
 * <p>
 * Cette interface définit les opérations de base pour exécuter et annuler une commande
 * dans le cadre du contrôleur de l’application. Elle permet de découpler
 * l’émetteur d’une action (par exemple l’interface utilisateur) de son exécution
 * réelle (le traitement métier).
 * </p>
 *
 * <p>Chaque implémentation concrète de cette interface représente une action
 * spécifique de l’application (ajout, suppression, modification, etc.).</p>
 *
 * @author [Votre nom]
 * @version 1.0
 */
public interface Commande {

    /**
     * Exécute la commande.
     * <p>
     * Cette méthode contient la logique à réaliser lorsqu’une commande est invoquée.
     * </p>
     */
    void executer();

    /**
     * Annule la commande.
     * <p>
     * Permet de revenir à l’état précédent avant l’exécution de la commande.
     * Cette méthode est utilisée notamment dans la gestion du système d’annulation (Undo).
     * </p>
     */
    void annuler();
}
