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
    private List<Chemin> anciensChemins;

    public CommandeAjoutLivraison(Tournee tournee, Carte carte, double vitesse,
                                  long idPickup, long idDelivery,
                                  long idPrecedentPickup, long idPrecedentDelivery) {
        this.tournee = tournee;
        this.carte = carte;
        this.vitesse = vitesse;
        this.idPickup = idPickup;
        this.idDelivery = idDelivery;
        this.idPrecedentPickup = idPrecedentPickup;
        this.idPrecedentDelivery = idPrecedentDelivery;
        this.anciensChemins = new ArrayList<>(tournee.getChemins());
    }

    @Override
    public void executer() {
        ModificationTournee modif = new ModificationTournee(new CalculChemins(carte), vitesse);
       // modif.ajouterLivraison(tournee, idPrecedent, idNoeud)
    }

    @Override
    public void annuler() {
        tournee.setChemins(new ArrayList<>(anciensChemins));
    }
}
