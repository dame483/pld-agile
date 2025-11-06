package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.erreurs.exception.TourneeNonConnexeException;
import fr.insalyon.pldagile.modele.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsable du calcul des tournees de livraison pour une demande donnee.
 * <p>
 * La classe utilise un algorithme en plusieurs etapes :
 * <ul>
 *   <li>Clustering des livraisons avec K-Means pour repartir les livraisons entre les livreurs.</li>
 *   <li>Calcul des plus courts chemins entre tous les nœuds d'un cluster avec Dijkstra.</li>
 *   <li>Resolution du problème du voyageur de commerce (TSP) exact ou heuristique selon la taille du cluster.</li>
 *   <li>Calcul des horaires de depart et d'arrivee pour chaque nœud de passage.</li>
 * </ul>
 * Elle prend en compte la vitesse des livreurs, le nombre de livreurs et l'heure de depart.
 */
public class CalculTournees {
    /**
     * Carte representant la ville.
     */
    private final Carte ville;

    /**
     * Demande de livraison contenant l'entrepôt et la liste des livraisons.
     */
    private final DemandeDeLivraison demande;

    /**
     * Vitesse des livreurs en mètres par seconde.
     */
    private final double vitesse;

    /**
     * Nombre de livreurs disponibles.
     */
    private final int nombreLivreurs;

    /**
     * Heure de depart des tournees.
     */
    private final LocalTime heureDepart;

    /**
     * Longueur totale cumulee d'une tournee en mètres.
     */
    private double longueurTotale = 0;

    /**
     * Duree totale cumulee d'une tournee en secondes.
     */
    private double dureeTotale = 0;

    /**
     * Taille maximale pour laquelle le TSP exact est utilise.
     */
    private static final int TAILLE_MAX_TSP_EXACT = 5;

    /**
     * Constructeur de la classe.
     *
     * @param ville        Carte de la ville
     * @param demande      Demande de livraison contenant les livraisons et l'entrepôt
     * @param vitesse      Vitesse des livreurs en m/s
     * @param nombreLivreurs Nombre de livreurs disponibles
     * @param heureDepart  Heure de depart des tournees
     */
    public CalculTournees(Carte ville, DemandeDeLivraison demande, double vitesse, int nombreLivreurs, LocalTime heureDepart) {
        this.ville = ville;
        this.demande = demande;
        this.vitesse = vitesse;
        this.nombreLivreurs = nombreLivreurs;
        this.heureDepart = heureDepart;
    }

    /**
     * Calcule les tournees pour tous les livreurs en respectant les contraintes de livraison.
     * <p>
     * Chaque tournee correspond à un cluster de livraisons assigne à un livreur.
     *
     * @return Liste des tournees calculees pour tous les livreurs
     * @throws TourneeNonConnexeException Si le graphe des chemins n'est pas connexe
     */
    public List<Tournee> calculerTournees() throws TourneeNonConnexeException {

        List<Livreur> livreurs = new ArrayList<>();
        for (int i = 0; i < nombreLivreurs; i++) {
            livreurs.add(new Livreur(i)); // IDs 0..n-1
        }


        List<Tournee> toutesLesTournees = new ArrayList<>();

        // etape 1 : Clustering K-Means
        KMeans kmeans = new KMeans(demande.getLivraisons(), nombreLivreurs);
        List<List<Livraison>> clusters = kmeans.cluster();

        // etape 2 : Calcul d’une tournee pour chaque cluster
        for (int i = 0; i < clusters.size(); i++) {
            List<Livraison> clusterLivraisons = clusters.get(i);
            if (clusterLivraisons == null || clusterLivraisons.isEmpty()) continue;

            DemandeDeLivraison demandeCluster = new DemandeDeLivraison(demande.getEntrepot(), clusterLivraisons);

            List<NoeudDePassage> noeuds = demandeCluster.getNoeudsDePassage();
            CalculChemins chemins = calculerPlusCourtsChemins(noeuds);
            Chemin[][] matriceChemins = chemins.getMatriceChemins();

            verifierConnexiteChemins(matriceChemins);

            GrapheComplet g = construireGrapheComplet(noeuds, matriceChemins);
            List<Integer> solution;

            // etape 3 : Choisir le bon algo
            if (clusterLivraisons.size() <= TAILLE_MAX_TSP_EXACT) {
                try {
                    solution = resoudreTSP(noeuds, clusterLivraisons, g);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                solution = resoudreTSPHeuristique(noeuds, clusterLivraisons, g);
            }

            List<Chemin> cheminsTournee = new ArrayList<>();
            LocalTime heureFin = calculerCheminsEtHoraires(solution, noeuds, matriceChemins, cheminsTournee);
            ajouterRetourEntrepot(solution, noeuds, matriceChemins, cheminsTournee, heureFin);

            Tournee tournee = new Tournee(cheminsTournee, dureeTotale, longueurTotale);
            tournee.setLivreur(livreurs.get(i));           // tournee → livreur
            livreurs.get(i).setTournee(tournee);           // livreur → tournee
            toutesLesTournees.add(tournee);

            longueurTotale = 0;
            dureeTotale = 0;
        }

        return toutesLesTournees;
    }


    /**
     * Resout le TSP exact avec contraintes de precedence pour un cluster donne.
     *
     * @param noeuds     Liste des nœuds de passage
     * @param livraisons Liste des livraisons du cluster
     * @param g          Graphe complet representant les distances entre les nœuds
     * @return Liste des indices representant l'ordre des nœuds dans la tournee
     * @throws InterruptedException si le calcul est interrompu
     */
    private List<Integer> resoudreTSP(List<NoeudDePassage> noeuds, List<Livraison> livraisons, GrapheComplet g) throws InterruptedException {
        TSPAvecPrecedence tsp = new TSPAvecPrecedence(noeuds, livraisons, g);
        tsp.resoudre(0);
        List<java.lang.Integer> solution = tsp.getSolution(0);
        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP exact n’a produit aucune solution !");
        }
        return solution;
    }

    /**
     * Resout le TSP heuristique avec contraintes de precedence pour un cluster donne.
     *
     * @param noeuds     Liste des nœuds de passage
     * @param livraisons Liste des livraisons du cluster
     * @param g          Graphe complet representant les distances entre les nœuds
     * @return Liste des indices representant l'ordre des nœuds dans la tournee
     */
    private List<Integer> resoudreTSPHeuristique(List<NoeudDePassage> noeuds, List<Livraison> livraisons, GrapheComplet g) {
        TSPHeuristiquePrecedence tsp = new TSPHeuristiquePrecedence(noeuds, livraisons, g);
        tsp.resoudre(0); // sommet depart = 0 (entrepôt)
        List<Integer> solution = tsp.getSolution(); // <-- plus de paramètre
        if (solution == null || solution.isEmpty()) {
            throw new RuntimeException("Le TSP heuristique n’a produit aucune solution !");
        }
        return solution;
    }

    /**
     * Calcule la matrice des plus courts chemins entre une liste de nœuds.
     *
     * @param noeuds Liste des nœuds de passage
     * @return Objet CalculChemins contenant les distances et chemins
     */
    private CalculChemins calculerPlusCourtsChemins(List<NoeudDePassage> noeuds) {
        CalculChemins chemins = new CalculChemins(ville);
        chemins.calculerMatrice(noeuds);
        return chemins;
    }

    /**
     * Construit un graphe complet à partir d'une matrice de chemins.
     *
     * @param noeuds         Liste des nœuds de passage
     * @param matriceChemins Matrice des chemins entre tous les nœuds
     * @return Graphe complet representant les distances entre tous les nœuds
     */
    private GrapheComplet construireGrapheComplet(List<NoeudDePassage> noeuds, Chemin[][] matriceChemins) {
        return new GrapheComplet(noeuds.size(), matriceChemins);
    }


    /**
     * Calcule les horaires de depart et d'arrivee pour chaque nœud d'une tournee
     * et accumule la longueur et la duree totale.
     *
     * @param solution       Liste des indices representant l'ordre des nœuds
     * @param noeuds         Liste des nœuds de passage
     * @param matriceChemins Matrice des chemins entre les nœuds
     * @param cheminsTournee Liste des chemins de la tournee à remplir
     * @return Heure d'arrivee finale de la tournee
     */
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

            depart.setHoraireDepart(heureCourante);
            double dureeTrajetSec = chemin.getLongueurTotal() / vitesse;
            LocalTime heureArrivee = heureCourante.plusSeconds(Math.round(dureeTrajetSec));
            arrivee.setHoraireArrivee(heureArrivee);

            heureCourante = heureArrivee.plusSeconds(Math.round(arrivee.getDuree()));

            int dernierIdx = solution.get(solution.size() - 1);
            int entrepotIdx = solution.get(0);
            if (dernierIdx != entrepotIdx) {arrivee.setHoraireDepart(heureCourante);}

            longueurTotale += chemin.getLongueurTotal();
            dureeTotale += dureeTrajetSec + arrivee.getDuree();
            cheminsTournee.add(chemin);
        }

        return heureCourante;
    }

    /**
     * Ajoute le chemin de retour à l'entrepôt pour une tournee.
     *
     * @param solution       Liste des indices representant l'ordre des nœuds
     * @param noeuds         Liste des nœuds de passage
     * @param matriceChemins Matrice des chemins entre les nœuds
     * @param cheminsTournee Liste des chemins de la tournee à completer
     * @param heureCourante  Heure d'arrivee au dernier nœud
     */
    private void ajouterRetourEntrepot(
            List<Integer> solution,
            List<NoeudDePassage> noeuds,
            Chemin[][] matriceChemins,
            List<Chemin> cheminsTournee,
            LocalTime heureCourante) {

        int dernierIdx = solution.get(solution.size() - 1);
        int entrepotIdx = solution.get(0);

        // Ne rien faire si on est dejà à l'entrepôt
        if (dernierIdx == entrepotIdx) return;

        Chemin retour = matriceChemins[dernierIdx][entrepotIdx];
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

    /**
     * Verifie que le graphe des chemins est connexe.
     *
     * @param matriceChemins Matrice des chemins entre tous les nœuds
     * @throws TourneeNonConnexeException Si un chemin est manquant ou infini entre deux nœuds
     */
    private void verifierConnexiteChemins(Chemin[][] matriceChemins) throws TourneeNonConnexeException {
        int n = matriceChemins.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matriceChemins[i][j] == null || matriceChemins[i][j].getLongueurTotal() == Double.POSITIVE_INFINITY) {
                    throw new TourneeNonConnexeException(
                            "Le graphe n'est pas connexe entre " + i + " et " + j
                    );
                }
            }
        }
    }
}
