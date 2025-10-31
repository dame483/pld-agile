package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

public class CommandeSuppressionLivraison implements Commande {

    private Tournee tournee;  // Référence partagée
    private Long idNoeudClique;
    private Long idNoeudAssocie;

    // Sauvegarde complète de l'état
    private List<Chemin> anciensChemins;
    private Carte carte;
    private double vitesse;

    public CommandeSuppressionLivraison(Tournee tournee, Carte carte, double vitesse,
                                        Long idNoeudClique, Long idNoeudAssocie) {
        this.tournee = tournee;
        this.idNoeudClique = idNoeudClique;
        this.idNoeudAssocie = idNoeudAssocie;
        this.carte = carte;
        this.vitesse = vitesse;
        this.anciensChemins= new ArrayList<>(tournee.getChemins());
    }

    @Override
    public void executer() {

        ModificationTournee modif = new ModificationTournee(new CalculChemins(carte), vitesse);

        // Suppression des deux nœuds (pickup et delivery)
        modif.supprimerNoeud(tournee, idNoeudClique);
        modif.supprimerNoeud(tournee, idNoeudAssocie);
    }

    @Override
    public void annuler() {
        // Restauration de l'état sauvegardé
        if (anciensChemins != null) {
            tournee.setChemins(new ArrayList<>(anciensChemins));
        }
    }
}