package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CalculTournee {

    private final Carte ville;
    private final DemandeDeLivraison demande;
    private final double vitesse; // m/s
    private double longueurTotale = 0;
    private double dureeTotale = 0;
    private final LocalTime heureDepart;

    public CalculTournee(Carte ville, DemandeDeLivraison demande, double vitesse, LocalTime heureDepart) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
        this.heureDepart = heureDepart;
    }

    public Tournee calculerTournee() throws Exception {
        List<NoeudDePassage> noeuds = demande.getNoeudsDePassage();
        List<Livraison> livraisons = demande.getLivraisons();

        //  CalculChemins: calcul des plus courts chemins
        CalculChemins calculchemins = new CalculChemins(ville);
        calculchemins.calculerMatrice(noeuds);
        Chemin[][] matriceChemins = calculchemins.getMatriceChemins();

        //  Graphe pour le TSP
        GrapheComplet g = new GrapheComplet(noeuds.size(), matriceChemins);

        //  Résolution TSP avec précédences
        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0);
        List<Integer> solution = tsp.getSolution(0);

        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP n’a produit aucune solution !");
        }

        //  Calcul des chemins et horaires
        List<Chemin> cheminsTournee = new ArrayList<>();
        LocalTime heureCourante = heureDepart; // départ exact de l’entrepôt

        // Entrepôt initialisation
        NoeudDePassage entrepot = noeuds.get(solution.get(0));
        entrepot.setHoraireDepart(heureDepart);

        for (int k = 0; k < solution.size() - 1; k++) {
            int idxDepart = solution.get(k);
            int idxArrivee = solution.get(k + 1);
            Chemin chemin = matriceChemins[idxDepart][idxArrivee];
            if (chemin == null) continue;

            NoeudDePassage depart = chemin.getNoeudDePassageDepart();
            NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();

            // Départ = heureCourante
            depart.setHoraireDepart(heureCourante);

            // Calcul de l'heure d'arrivée
            double dureeTrajetSec = chemin.getLongueurTotal() / vitesse;
            LocalTime heureArrivee = heureCourante.plusSeconds(Math.round(dureeTrajetSec));
            arrivee.setHoraireArrivee(heureArrivee);

            // Heure de départ du noeud = arrivée + durée de service (sauf pour l’entrepôt)
            heureCourante = heureArrivee.plusSeconds(Math.round(arrivee.getDuree()));
            arrivee.setHoraireDepart(heureCourante);

            longueurTotale += chemin.getLongueurTotal();
            dureeTotale += dureeTrajetSec + arrivee.getDuree();

            cheminsTournee.add(chemin);
        }

        //  Retour à l’entrepôt
        int dernierIdx = solution.get(solution.size() - 1);
        Chemin retour = matriceChemins[dernierIdx][solution.get(0)];
        if (retour != null) {
            NoeudDePassage depart = retour.getNoeudDePassageDepart();
            NoeudDePassage arrivee = retour.getNoeudDePassageArrivee();

            depart.setHoraireDepart(heureCourante);

            double dureeTrajetRetour = retour.getLongueurTotal() / vitesse;
            LocalTime heureArriveeFinale = heureCourante.plusSeconds(Math.round(dureeTrajetRetour));
            arrivee.setHoraireArrivee(heureArriveeFinale);

            // Entrepôt : départ = heureDepart initial, arrivée = fin de tournée
            entrepot.setHoraireArrivee(heureArriveeFinale);

            longueurTotale += retour.getLongueurTotal();
            dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);

            cheminsTournee.add(retour);
        }

        return new Tournee(cheminsTournee, dureeTotale);
    }

    public double getLongueurTotale() {
        return longueurTotale;
    }

    public double getDureeTotale() {
        return dureeTotale;
    }
}