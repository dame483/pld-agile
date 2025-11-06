package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation de la commande {@link Commande} permettant d’ajouter une livraison
 * (un couple enlèvement / dépôt) dans une {@link Tournee}.
 * <p>
 * Cette commande suit le patron de conception Command (Commande) et permet
 * d’exécuter une modification de la tournée tout en conservant la possibilité
 * d’annuler l’opération pour revenir à l’état précédent.
 * </p>
 *
 * <p>
 * Le processus d’exécution :
 * <ul>
 *     <li>Avant toute modification, l’état actuel de la tournée (liste des chemins)
 *     est sauvegardé dans {@code etatAvantExecution}.</li>
 *     <li>Si c’est la première exécution, la commande calcule les nouveaux chemins
 *     après insertion du point d’enlèvement et du point de livraison.</li>
 *     <li>Si la commande a déjà été exécutée, elle restaure simplement
 *     l’état sauvegardé après l’ajout.</li>
 * </ul>
 *
 *
 *
 * En cas d’annulation, la tournée est restaurée à son état avant exécution.
 * 
 *
 * @see Commande
 * @see ModificationTournee
 * @see Tournee
 * @see Chemin
 * @see Carte
 */
public class CommandeAjoutLivraison implements Commande {
    private final Tournee tournee;
    private final Carte carte;
    private final double vitesse;
    private final long idPickup, idDelivery, idPrecedentPickup, idPrecedentDelivery;
    private final double dureeEnlevement;
    private final double dureeLivraison;

    private List<Chemin> etatAvantExecution;
    private List<Chemin> etatApresExecution;


    /**
     * Construit une commande d’ajout de livraison.
     *
     * @param tournee              La tournée à modifier.
     * @param carte                La carte sur laquelle les calculs de chemins seront effectués.
     * @param vitesse              La vitesse moyenne du livreur (en m/s).
     * @param idPickup             L’identifiant du nœud correspondant à l’enlèvement.
     * @param idDelivery           L’identifiant du nœud correspondant à la livraison.
     * @param idPrecedentPickup    L’identifiant du nœud précédant l’enlèvement.
     * @param idPrecedentDelivery  L’identifiant du nœud précédant la livraison.
     * @param dureeEnlevement      La durée d’enlèvement (en secondes).
     * @param dureeLivraison       La durée de livraison (en secondes).
     */
    public CommandeAjoutLivraison(Tournee tournee, Carte carte, double vitesse,
                                  long idPickup, long idDelivery,
                                  long idPrecedentPickup, long idPrecedentDelivery,
                                  double dureeEnlevement, double dureeLivraison) {
        this.tournee = tournee;
        this.carte = carte;
        this.vitesse = vitesse;
        this.idPickup = idPickup;
        this.idDelivery = idDelivery;
        this.idPrecedentPickup = idPrecedentPickup;
        this.idPrecedentDelivery = idPrecedentDelivery;
        this.dureeEnlevement = dureeEnlevement;
        this.dureeLivraison = dureeLivraison;

    }

    /**
     * Exécute la commande d’ajout de livraison.
     * <p>
     * Si c’est la première exécution, la commande ajoute les nœuds de
     * l’enlèvement et de la livraison dans la tournée via {@link ModificationTournee}.
     * Sinon, elle restaure simplement l’état déjà calculé.
     * </p>
     */
    @Override
    public void executer() {

        etatAvantExecution = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();

        if (etatApresExecution == null) {

            ModificationTournee modif = new ModificationTournee(new CalculChemins(carte), vitesse);
            modif.ajouterNoeudPickup(tournee, idPickup, idPrecedentPickup, dureeEnlevement);
            modif.ajouterNoeudDelivery(tournee, idDelivery, idPrecedentDelivery, dureeLivraison);

            etatApresExecution = tournee.getChemins().stream()
                    .map(Chemin::copieProfonde)
                    .toList();
        } else {
            List<Chemin> cheminsRestores = etatApresExecution.stream()
                    .map(Chemin::copieProfonde)
                    .toList();
            tournee.setChemins(new ArrayList<>(cheminsRestores));
        }
    }

    /**
     * Annule la commande d’ajout de livraison.
     * <p>
     * Cette méthode restaure la tournée à l’état qu’elle avait avant
     * l’exécution de la commande.
     * </p>
     */
    @Override
    public void annuler() {
        if (etatAvantExecution != null) {
            List<Chemin> cheminsRestores = etatAvantExecution.stream()
                    .map(Chemin::copieProfonde)
                    .toList();
            tournee.setChemins(new ArrayList<>(cheminsRestores));
        }
    }
}