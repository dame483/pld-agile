package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

public class CommandeSuppressionLivraison implements Commande {

    private Tournee tournee;
    private Long idNoeudClique;
    private Long idNoeudAssocie;
    private List<Chemin> anciensChemins;
    private Carte carte;
    private double vitesse;


    /**
     * Commande pour supprimer une livraison complète (pickup + delivery) d'une tournée.
     *
     */
    public CommandeSuppressionLivraison(Tournee tournee, Carte carte, double vitesse, Long idNoeudClique, Long idNoeudAssocie) {
        this.tournee = tournee;
        this.idNoeudClique = idNoeudClique;
        this.idNoeudAssocie = idNoeudAssocie;
        this.carte = carte;
        this.vitesse = vitesse;
    }

    @Override
    public void executer() {
        // Sauvegarde de l'état complet de la tournée
        anciensChemins = new ArrayList<>(tournee.getChemins());

        // Utilisation de la classe ModificationTournee pour supprimer le pickup et le delivery
        ModificationTournee modif = new ModificationTournee(new CalculChemins(carte), vitesse);
        tournee = modif.supprimerNoeud(tournee, idNoeudClique);
        tournee = modif.supprimerNoeud(tournee, idNoeudAssocie);
    }

    @Override
    public void annuler() {
        // On restaure exactement l'état de la tournée avant suppression
        if (anciensChemins != null) {
            tournee.setChemins(new ArrayList<>(anciensChemins));
        }
    }

}
