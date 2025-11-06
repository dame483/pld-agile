package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande permettant de supprimer une livraison d'une tournée.
 */
public class CommandeSuppressionLivraison implements Commande {
    /** Tournée sur laquelle l'opération est effectuée. */
    private Tournee tournee;

    /** ID du noeud de livraison cliqué pour suppression. */
    private Long idNoeudClique;

    /** ID du noeud associé à la livraison. */
    private Long idNoeudAssocie;

    /** État des chemins avant l'exécution de la commande. */
    private List<Chemin> etatAvantExecution;

    /** État des chemins après l'exécution de la commande. */
    private List<Chemin> etatApresExecution;

    /** Carte utilisée pour recalculer les chemins. */
    private Carte carte;

    /** Vitesse utilisée pour le recalcul des chemins. */
    private double vitesse;

    /**
     * Crée une commande de suppression de livraison.
     *
     * @param tournee Tournée cible.
     * @param carte Carte routière utilisée.
     * @param vitesse Vitesse pour le calcul des chemins.
     * @param idNoeudClique Noeud cliqué pour suppression.
     * @param idNoeudAssocie Noeud associé à la livraison.
     */
    public CommandeSuppressionLivraison(Tournee tournee, Carte carte, double vitesse,
                                        Long idNoeudClique, Long idNoeudAssocie) {
        this.tournee = tournee;
        this.idNoeudClique = idNoeudClique;
        this.idNoeudAssocie = idNoeudAssocie;
        this.carte = carte;
        this.vitesse = vitesse;
    }

    /** Exécute la commande de suppression. */
    @Override
    public void executer() {
        etatAvantExecution = tournee.getChemins().stream()
                .map(Chemin::copieProfonde)
                .toList();

        if (etatApresExecution == null) {
            ModificationTournee modif = new ModificationTournee(new CalculChemins(carte), vitesse);
            modif.supprimerNoeud(tournee, idNoeudClique);
            modif.supprimerNoeud(tournee, idNoeudAssocie);

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

    /** Annule la suppression et restaure l'état précédent. */
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