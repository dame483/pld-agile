package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.exception.TourneeNonConnexeException;
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

    public Tournee calculerTournee() throws TourneeNonConnexeException {
        List<NoeudDePassage> noeuds = demande.getNoeudsDePassage();
        CalculChemins chemins = calculerPlusCourtsChemins(noeuds);
        Chemin[][] matriceChemins = chemins.getMatriceChemins();

        verifierConnexiteChemins(matriceChemins);

        GrapheComplet g = construireGrapheComplet(noeuds, matriceChemins);
        List<Integer> solution = resoudreTSP(noeuds, demande.getLivraisons(), g);

        List<Chemin> cheminsTournee = new ArrayList<>();
        LocalTime heureFin = calculerCheminsEtHoraires(solution, noeuds, matriceChemins, cheminsTournee);
        ajouterRetourEntrepot(solution, noeuds, matriceChemins, cheminsTournee, heureFin);

        return new Tournee(cheminsTournee, dureeTotale);
    }



    private CalculChemins calculerPlusCourtsChemins(List<NoeudDePassage> noeuds) {
        CalculChemins chemins = new CalculChemins(ville);
        chemins.calculerMatrice(noeuds);
        return chemins;
    }



    private GrapheComplet construireGrapheComplet(List<NoeudDePassage> noeuds, Chemin[][] matriceChemins) {
        GrapheComplet g = new GrapheComplet(noeuds.size(), matriceChemins);
        return g;
    }



    private List<Integer> resoudreTSP(List<NoeudDePassage> noeuds, List<Livraison> livraisons, GrapheComplet g) {
        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0);
        List<Integer> solution = tsp.getSolution(0);
        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP n’a produit aucune solution !");
        }
        return solution;
    }



    private LocalTime calculerCheminsEtHoraires(
            List<Integer> solution,
            List<NoeudDePassage> noeuds,
            Chemin[][] matriceChemins,
            List<Chemin> cheminsTournee) {

        LocalTime heureCourante = heureDepart;
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

            // Temps de trajet
            double dureeTrajetSec = chemin.getLongueurTotal() / vitesse;
            LocalTime heureArrivee = heureCourante.plusSeconds(Math.round(dureeTrajetSec));
            arrivee.setHoraireArrivee(heureArrivee);

            // Pause sur place (livraison/ramassage)
            heureCourante = heureArrivee.plusSeconds(Math.round(arrivee.getDuree()));
            arrivee.setHoraireDepart(heureCourante);

            longueurTotale += chemin.getLongueurTotal();
            dureeTotale += dureeTrajetSec + arrivee.getDuree();
            cheminsTournee.add(chemin);
        }

        return heureCourante;
    }




    private void ajouterRetourEntrepot(
            List<Integer> solution,
            List<NoeudDePassage> noeuds,
            Chemin[][] matriceChemins,
            List<Chemin> cheminsTournee,
            LocalTime heureCourante) {

        int dernierIdx = solution.get(solution.size() - 1);
        Chemin retour = matriceChemins[dernierIdx][solution.get(0)];
        if (retour == null) return;

        NoeudDePassage depart = retour.getNoeudDePassageDepart();
        NoeudDePassage arrivee = retour.getNoeudDePassageArrivee();

        depart.setHoraireDepart(heureCourante);

        double dureeTrajetRetour = retour.getLongueurTotal() / vitesse;
        LocalTime heureArriveeFinale = heureCourante.plusSeconds(Math.round(dureeTrajetRetour));

        arrivee.setHoraireArrivee(heureArriveeFinale);
        noeuds.get(0).setHoraireArrivee(heureArriveeFinale);

        longueurTotale += retour.getLongueurTotal();
        dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);

        cheminsTournee.add(retour);
    }




    private void afficherDebugFloydWarshall(CalculChemins floyd, List<NoeudDePassage> noeuds) {
        double[][] distances = floyd.getDistances();
        Chemin[][] chemins = floyd.getMatriceChemins();

        int cellWidth = 12;
        System.out.println("=== Matrice des distances (en mètres) ===");
        System.out.printf("%" + cellWidth + "s", "");
        for (NoeudDePassage n : noeuds) {
            System.out.printf("%" + cellWidth + "s", n.getId());
        }
        System.out.println();

        for (int i = 0; i < distances.length; i++) {
            System.out.printf("%" + cellWidth + "s", noeuds.get(i).getId());
            for (int j = 0; j < distances[i].length; j++) {
                if (distances[i][j] == Double.POSITIVE_INFINITY) {
                    System.out.printf("%" + cellWidth + "s", "INF");
                } else {
                    System.out.printf("%" + cellWidth + ".1f", distances[i][j]);
                }
            }
            System.out.println();
        }

        System.out.println("\n=== Matrice des chemins : Ordre des tronçons ===");
        for (int i = 0; i < chemins.length; i++) {
            for (int j = 0; j < chemins[i].length; j++) {
                Chemin c = chemins[i][j];
                if (c == null || c.getLongueurTotal() == Double.POSITIVE_INFINITY) {
                    System.out.print("INF\t");
                } else {
                    String tronconsStr = c.getTroncons().stream()
                            .map(Troncon::getnomRue)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    System.out.print("[" + tronconsStr + "]\t");
                }
            }
            System.out.println();
        }
    }



    public double getLongueurTotale() {
        return longueurTotale;
    }

    public double getDureeTotale() {
        return dureeTotale;
    }

    /**
     * Vérifie que tous les noeuds de passage sont connectés entre eux.
     * @param matriceChemins la matrice des chemins calculée par CalculChemins
     * @throws Exception si un chemin est manquant ou infini (graphe non connexe)
     */
    private void verifierConnexiteChemins(Chemin[][] matriceChemins) throws TourneeNonConnexeException {
        int n = matriceChemins.length;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if (matriceChemins[i][j] == null || matriceChemins[i][j].getLongueurTotal() == Double.POSITIVE_INFINITY) {
                    throw new TourneeNonConnexeException(
                            "La tournée ne peut pas être calculée : le graphe des chemins n'est pas connexe entre "
                                    + i + " et " + j
                    );
                }
            }
        }
    }


}