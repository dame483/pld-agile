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

    private List<Chemin> etatAvantExecution;
    private List<Chemin> etatApresExecution;

    private Carte carte;
    private double vitesse;

    public CommandeSuppressionLivraison(Tournee tournee, Carte carte, double vitesse,
                                        Long idNoeudClique, Long idNoeudAssocie) {
        this.tournee = tournee;
        this.idNoeudClique = idNoeudClique;
        this.idNoeudAssocie = idNoeudAssocie;
        this.carte = carte;
        this.vitesse = vitesse;
    }

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