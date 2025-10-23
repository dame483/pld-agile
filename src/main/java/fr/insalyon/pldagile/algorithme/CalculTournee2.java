package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CalculTournee2 {

    private final Carte ville;
    private final DemandeDeLivraison demande;
    private final double vitesse; // m/s
    private double longueurTotale = 0;
    private double dureeTotale = 0;
    private final LocalTime heureDepart;

    public CalculTournee2(Carte ville, DemandeDeLivraison demande, double vitesse, LocalTime heureDepart) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
        this.heureDepart = heureDepart;
    }

    public Tournee calculerTournee() throws Exception {
        List<NoeudDePassage> noeuds = demande.getNoeudsDePassage();
        List<Livraison> livraisons = demande.getLivraisons();

        // CalculChemins pour les plus courts chemins
        CalculChemins calculChemins = new CalculChemins(ville);
        calculChemins.calculerMatrice(noeuds);
        Chemin[][] matriceChemins = calculChemins.getMatriceChemins();

        // Graphe complet des distances
        GrapheComplet g = new GrapheComplet(noeuds.size(),matriceChemins);


        // Création de la map precedences : delivery -> pickup
        Map<Integer, Integer> precedences = new HashMap<>();
        for (Livraison l : livraisons) {
            int pickupIdx = noeuds.indexOf(l.getAdresseEnlevement());
            int deliveryIdx = noeuds.indexOf(l.getAdresseLivraison());
            precedences.put(deliveryIdx, pickupIdx);
        }

        // Calcul heuristique de la tournée
        TSPHeuristique tspHeur = new TSPHeuristique(noeuds, g, precedences);
        int departIdx = noeuds.indexOf(demande.getEntrepot());
        List<Integer> solution = tspHeur.calculerTourneeHeuristique(departIdx);

        List<Chemin> cheminsTournee = new ArrayList<>();
        LocalTime heureCourante = heureDepart;
        NoeudDePassage entrepot = noeuds.get(departIdx);
        entrepot.setHoraireDepart(heureDepart);

        for (int k = 0; k < solution.size() - 1; k++) {
            int idxDepart = solution.get(k);
            int idxArrivee = solution.get(k + 1);
            Chemin chemin = matriceChemins[idxDepart][idxArrivee];
            if (chemin == null) continue;

            NoeudDePassage depart = chemin.getNoeudDePassageDepart();
            NoeudDePassage arrivee = chemin.getNoeudDePassageArrivee();

            depart.setHoraireDepart(heureCourante);

            double dureeTrajetSec = chemin.getLongueurTotal() / vitesse;
            LocalTime heureArrivee = heureCourante.plusSeconds(Math.round(dureeTrajetSec));
            arrivee.setHoraireArrivee(heureArrivee);

            // Heure de départ = arrivée + durée de service (sauf entrepôt)
            heureCourante = heureArrivee.plusSeconds(Math.round(arrivee.getDuree()));
            if (arrivee.getType() != NoeudDePassage.TypeNoeud.ENTREPOT)
                arrivee.setHoraireDepart(heureCourante);

            longueurTotale += chemin.getLongueurTotal();
            dureeTotale += dureeTrajetSec + arrivee.getDuree();

            cheminsTournee.add(chemin);
        }

        // Retour à l’entrepôt : finalisation horaires
        int dernierIdx = solution.get(solution.size() - 1);
        if (dernierIdx != departIdx) {
            Chemin retour = matriceChemins[dernierIdx][departIdx];
            if (retour != null) {
                NoeudDePassage depart = retour.getNoeudDePassageDepart();
                NoeudDePassage arrivee = retour.getNoeudDePassageArrivee();
                depart.setHoraireDepart(heureCourante);

                double dureeTrajetRetour = retour.getLongueurTotal() / vitesse;
                LocalTime heureArriveeFinale = heureCourante.plusSeconds(Math.round(dureeTrajetRetour));
                entrepot.setHoraireArrivee(heureArriveeFinale);

                longueurTotale += retour.getLongueurTotal();
                dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);

                cheminsTournee.add(retour);
            }
        } else {
            // Si dernière étape = entrepôt, simplement définir heure d'arrivée
            entrepot.setHoraireArrivee(heureCourante);
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
