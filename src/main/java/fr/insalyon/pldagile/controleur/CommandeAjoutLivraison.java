package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;

import java.util.ArrayList;
import java.util.List;

public class CommandeAjoutLivraison implements Commande {
    private final Tournee tournee;
    private final Carte carte;
    private final double vitesse;
    private final long idPickup, idDelivery, idPrecedentPickup, idPrecedentDelivery;
    private final double dureeEnlevement;
    private final double dureeLivraison;

    private List<Chemin> etatAvantExecution;
    private List<Chemin> etatApresExecution;

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