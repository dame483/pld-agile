package fr.insalyon.pldagile;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SuppressionCheminsTourneeTests {

    @Test
    public void testGetNoeudParId() throws Exception {

        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");


        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");


        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16; // m/s
        int nombreLivreurs = 1;


        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);
        assertNotNull(tournee, "Une tournée doit être récupérée");


        for (Chemin c : tournee.getChemins()) {
            long idDepart = c.getNoeudDePassageDepart().getId();
            long idArrivee = c.getNoeudDePassageArrivee().getId();

            NoeudDePassage n1 = tournee.getNoeudParId(idDepart);
            NoeudDePassage n2 = tournee.getNoeudParId(idArrivee);

            assertNotNull(n1, "Le noeud de départ doit être trouvé");
            assertNotNull(n2, "Le noeud d'arrivée doit être trouvé");

            assertEquals(idDepart, n1.getId(), "Les IDs doivent correspondre pour le départ");
            assertEquals(idArrivee, n2.getId(), "Les IDs doivent correspondre pour l'arrivée");
        }


        NoeudDePassage absent = tournee.getNoeudParId(-1);
        assertNull(absent, "Un noeud inexistant doit retourner null");
    }

    @Test
    public void testTrouverCheminsAvantApresNoeud() throws Exception {
        // Chargement des données
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16; // m/s
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        NoeudDePassage n = tournee.getChemins().get(1).getNoeudDePassageArrivee();

        Chemin avant = null, apres = null;
        for (Chemin c : tournee.getChemins()) {
            if (c.getNoeudDePassageArrivee().equals(n)) avant = c;
            if (c.getNoeudDePassageDepart().equals(n)) apres = c;
        }

        assertNotNull(avant, "Un chemin doit arriver sur le nœud sélectionné");
        assertNotNull(apres, "Un chemin doit partir du nœud sélectionné");

        System.out.println("Noeud sélectionné : " + n.getId());
        System.out.println("Chemin avant : " + avant);
        System.out.println("Chemin après : " + apres);

        assertEquals(n, avant.getNoeudDePassageArrivee());
        assertEquals(n, apres.getNoeudDePassageDepart());
        assertEquals(avant.getNoeudDePassageArrivee(), apres.getNoeudDePassageDepart());
    }

    @Test
    public void testSuppressionCheminsAutourNoeud() throws Exception {
        // Chargement des données
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);
        int tailleAvantSuppression = tournee.getChemins().size();

        // Choisir un nœud intermédiaire
        NoeudDePassage n = tournee.getChemins().get(1).getNoeudDePassageArrivee();

        // Trouver les chemins avant et après ce nœud
        Chemin avant = null, apres = null;
        for (Chemin c : tournee.getChemins()) {
            if (c.getNoeudDePassageArrivee().equals(n)) avant = c;
            if (c.getNoeudDePassageDepart().equals(n)) apres = c;
        }

        assertNotNull(avant, "Le chemin avant doit être trouvé");
        assertNotNull(apres, "Le chemin après doit être trouvé");

        // Supprimer les deux chemins de la tournée
        Chemin finalAvant = avant;
        Chemin finalApres = apres;
        tournee.getChemins().removeIf(c -> c.equals(finalAvant) || c.equals(finalApres));

        // Vérifications après suppression
        int tailleApresSuppression = tournee.getChemins().size();
        assertEquals(tailleAvantSuppression - 2, tailleApresSuppression,
                "Deux chemins (avant et après le nœud supprimé) doivent être retirés de la tournée");

        assertFalse(tournee.getChemins().contains(avant), "Le chemin avant ne doit plus être présent");
        assertFalse(tournee.getChemins().contains(apres), "Le chemin après ne doit plus être présent");

        System.out.println("Suppression réussie : chemins avant/après supprimés pour le nœud " + n.getId());

    }

    @Test
    public void testCalculCheminPlusCourtEntreDeuxNoeuds() throws Exception {
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        NoeudDePassage n1 = tournee.getNoeudParId(1679901320);
        NoeudDePassage n2 = tournee.getNoeudParId(208769457);

        CalculChemins calculChemins = new CalculChemins(ville);
        Chemin chemin = calculChemins.calculerCheminPlusCourt(n1, n2);

        assertNotNull(chemin, "Le chemin entre n1 et n2 doit être trouvé");
        assertFalse(chemin.getTroncons().isEmpty(), "Le chemin doit contenir des tronçons");

        System.out.println("Chemin calculé : " + chemin);

        // === Vérification détaillée du résultat attendu ===
        assertEquals(1679901320, chemin.getNoeudDePassageDepart().getId());
        assertEquals(208769457, chemin.getNoeudDePassageArrivee().getId());

        // Tolérance sur la longueur à cause des arrondis
        assertEquals(411.13, chemin.getLongueurTotal(), 0.5, "La longueur du chemin doit être correcte");

        // Vérifie les noms de rues dans l'ordre
        List<String> nomsRues = chemin.getTroncons()
                .stream()
                .map(Troncon::getNomRue)
                .toList();

        List<String> attendu = List.of("Rue Pascal", "Rue Lafontaine", "Rue Édouard Aynard", "Rue Frédéric Passy");

        assertEquals(attendu, nomsRues, "Les rues empruntées doivent correspondre au chemin attendu");
    }

    @Test
    public void testInsertionCheminAuBonEndroit() throws Exception {
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        // === On choisit un noeud à supprimer et à remplacer par un chemin direct ===
        NoeudDePassage n = tournee.getNoeudParId(208769457);
        assertNotNull(n, "Le nœud à supprimer doit exister dans la tournée");

        // Repérer les chemins avant et après ce nœud
        Chemin avant = null, apres = null;
        for (Chemin c : tournee.getChemins()) {
            if (c.getNoeudDePassageArrivee().equals(n)) avant = c;
            if (c.getNoeudDePassageDepart().equals(n)) apres = c;
        }

        assertNotNull(avant, "Il doit exister un chemin avant le nœud supprimé");
        assertNotNull(apres, "Il doit exister un chemin après le nœud supprimé");

        NoeudDePassage precedent = avant.getNoeudDePassageDepart();
        NoeudDePassage suivant = apres.getNoeudDePassageArrivee();

        int index = 0;
        if (avant != null) {
            index = tournee.getChemins().indexOf(avant);
        }

        Chemin finalAvant = avant;
        Chemin finalApres = apres;
        tournee.getChemins().removeIf(c -> c.equals(finalAvant) || c.equals(finalApres));

        CalculChemins calculChemins = new CalculChemins(ville);
        Chemin nouveauChemin = calculChemins.calculerCheminPlusCourt(precedent, suivant);

        tournee.getChemins().add(index, nouveauChemin);

        List<Chemin> chemins = tournee.getChemins();

        assertTrue(chemins.contains(nouveauChemin), "Le nouveau chemin doit être présent dans la tournée");

        int indexNouveau = chemins.indexOf(nouveauChemin);

        if (indexNouveau > 0) {
            Chemin precedentDansListe = chemins.get(indexNouveau - 1);
            assertEquals(precedent, precedentDansListe.getNoeudDePassageArrivee(),
                    "Le nœud d'arrivée du chemin précédent doit correspondre au départ du nouveau chemin");
        }

        // Et le suivant doit commencer là où le nouveau chemin s’arrête
        if (indexNouveau < chemins.size() - 1) {
            Chemin suivantDansListe = chemins.get(indexNouveau + 1);
            assertEquals(suivant, suivantDansListe.getNoeudDePassageDepart(),
                    "Le nœud de départ du chemin suivant doit correspondre à l'arrivée du nouveau chemin");
        }

    }

    @Test
    public void testSuppressionNoeud() throws Exception {
        // Charger la carte
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        // Charger la demande
        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        // Paramètres de calcul
        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);
        double vitesse = 4.16;
        int nombreLivreurs = 1;

        // Calculer les tournées
        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        // Vérifier que le noeud à supprimer existe dans la tournée
        long idNoeudASupprimer = 208769457;
        NoeudDePassage n = tournee.getNoeudParId(idNoeudASupprimer);
        assertNotNull(n, "Le noeud à supprimer doit exister dans la tournée");

        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);

        // Supprimer le noeud
        Tournee tourneeModifiee = modificationTournee.supprimerNoeud(tournee, idNoeudASupprimer);
        assertNotNull(tourneeModifiee, "La tournée modifiée ne doit pas être null");

        // Vérifier que le noeud supprimé n'apparaît plus dans aucun chemin
        for (Chemin c : tourneeModifiee.getChemins()) {
            assertNotEquals(idNoeudASupprimer, c.getNoeudDePassageDepart().getId(),
                    "Le noeud supprimé ne doit plus être le départ d'un chemin");
            assertNotEquals(idNoeudASupprimer, c.getNoeudDePassageArrivee().getId(),
                    "Le noeud supprimé ne doit plus être l'arrivée d'un chemin");
        }

    }

    @Test
    public void testLongueurEtDureeTotaleApresModification() throws Exception {
        // Préparer carte, demande et calcul des tournées
        File fichierCarte = new File("src/main/resources/donnees/plans/moyenPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);

        LocalTime heureDepart = LocalTime.of(8, 0);
        double vitesse = 4.16;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, 1, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        Tournee tournee = toutesLesTournees.get(0);

        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);

        // Calculer la longueur et durée totales réelles
        double longueurTotale = 0;
        double dureeTotale = 0;
        for (Chemin c : tournee.getChemins()) {
            longueurTotale += c.getLongueurTotal();
            dureeTotale += c.getLongueurTotal() / vitesse;           // durée trajet
            dureeTotale += c.getNoeudDePassageArrivee().getDuree(); // durée service
        }

        // Vérifier que les valeurs de l'objet Tournee correspondent
        assertEquals(longueurTotale, tournee.getLongueurTotale(), 0.1, "Longueur totale incorrecte");
        assertEquals(dureeTotale, tournee.getDureeTotale(), 1.0, "Durée totale incorrecte");
    }

    @Test
    public void testSuppressionDeuxNoeud() throws Exception {
        // Charger la carte
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        // Charger la demande
        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        // Paramètres de calcul
        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);
        double vitesse = 4.16;
        int nombreLivreurs = 1;

        // Calculer les tournées
        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        // Vérifier que le noeud à supprimer existe dans la tournée
        long idNoeudASupprimer = 208769457;
        NoeudDePassage n = tournee.getNoeudParId(idNoeudASupprimer);
        assertNotNull(n, "Le noeud à supprimer doit exister dans la tournée");

        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);

        // Supprimer le noeud
        Tournee tourneeModifiee = modificationTournee.supprimerNoeud(tournee, idNoeudASupprimer);
        assertNotNull(tourneeModifiee, "La tournée modifiée ne doit pas être null");

        Tournee tourneeFinal = modificationTournee.supprimerNoeud(tourneeModifiee, 1679901320);

        // Vérifier que le noeud supprimé n'apparaît plus dans aucun chemin
        for (Chemin c : tourneeFinal.getChemins()) {
            assertNotEquals(idNoeudASupprimer, c.getNoeudDePassageDepart().getId(),
                    "Le noeud supprimé ne doit plus être le départ d'un chemin");
            assertNotEquals(idNoeudASupprimer, c.getNoeudDePassageArrivee().getId(),
                    "Le noeud supprimé ne doit plus être l'arrivée d'un chemin");
        }

        // Affichage simplifié pour vérification manuelle
        System.out.println("Tournée après suppression du noeud " + idNoeudASupprimer + " :");
        for (int i = 0; i < tourneeFinal.getChemins().size(); i++) {
            Chemin c = tourneeFinal.getChemins().get(i);
            System.out.printf("Chemin %d : %d -> %d, longueur=%.0f m\n",
                    i + 1,
                    c.getNoeudDePassageDepart().getId(),
                    c.getNoeudDePassageArrivee().getId(),
                    c.getLongueurTotal());
        }
    }

    @Test
    public void testSupprimerNoeudIgnoreEntrepot() throws Exception {
        File fichierCarte = new File("src/main/resources/donnees/plans/moyenPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville);

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande);

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty());

        Tournee tournee = toutesLesTournees.get(0);

        // récupère l'entrepôt
        NoeudDePassage entrepot = tournee.getChemins().get(0).getNoeudDePassageDepart();

        // appel à supprimerNoeud avec l'entrepôt
        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);
        Tournee result = modificationTournee.supprimerNoeud(tournee, entrepot.getId());

        // vérifier que la tournée n'a pas changé
        assertEquals(tournee.getChemins().size(), result.getChemins().size(), "La taille de la tournée ne doit pas changer");
        assertEquals(entrepot, result.getChemins().get(0).getNoeudDePassageDepart(), "Le premier noeud doit rester l'entrepôt");
    }

    @Test
    public void testSupprimerNoeudIdInexistant() throws Exception {
        File fichierCarte = new File("src/main/resources/donnees/plans/moyenPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville);

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande);

        LocalTime heureDepart = (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null)
                ? demande.getEntrepot().getHoraireDepart()
                : LocalTime.of(8, 0);

        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty());

        Tournee tournee = toutesLesTournees.get(0);

        // ID inexistant
        long idInexistant = 999999999L;

        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);
        Tournee result = modificationTournee.supprimerNoeud(tournee, idInexistant);

        // Vérifie que la tournée est inchangée
        assertEquals(tournee.getChemins().size(), result.getChemins().size(), "La taille de la tournée doit rester identique");
        assertEquals(tournee, result, "La tournée doit rester identique si le noeud n'existe pas");
    }

    @Test
    public void testSuppressionNoeudEtHoraires() throws Exception {
        // Charger la carte et la demande
        File fichierCarte = new File("src/main/resources/donnees/plans/petitPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        assertNotNull(ville, "La carte doit être chargée");

        File fichierDemande = new File("src/main/resources/donnees/demandes/demandePetit2.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        assertNotNull(demande, "La demande doit être chargée");

        LocalTime heureDepart = demande.getEntrepot().getHoraireDepart();
        double vitesse = 4.16;
        int nombreLivreurs = 1;

        CalculTournees calculTournees = new CalculTournees(ville, demande, vitesse, nombreLivreurs, heureDepart);
        List<Tournee> toutesLesTournees = calculTournees.calculerTournees();
        assertFalse(toutesLesTournees.isEmpty(), "Au moins une tournée doit être calculée");

        Tournee tournee = toutesLesTournees.get(0);

        // Supprimer le nœud 208769457
        ModificationTournee modif = new ModificationTournee(new CalculChemins(ville), vitesse);
        tournee = modif.supprimerNoeud(tournee, 208769457);

        List<Chemin> chemins = tournee.getChemins();

        // Ordre attendu après suppression
        long[] ordreAttendu = {2835339774L, 208769120L, 1679901320L, 25336179L, 2835339774L};
        double[] distancesAttendu = {447, 866, 1804, 1115};
        String[] horairesDepartAttendu = {"08:00:00", "08:08:47", "08:19:15", "08:34:28"};
        String[] horairesArriveeAttendu = {"08:01:47", "08:12:15", "08:26:28", "08:38:56"};

        assertEquals(distancesAttendu.length, chemins.size(), "Nombre de chemins après suppression");

        for (int i = 0; i < chemins.size(); i++) {
            Chemin c = chemins.get(i);

            long departId = c.getNoeudDePassageDepart().getId();
            long arriveeId = c.getNoeudDePassageArrivee().getId();

            assertEquals(ordreAttendu[i], departId, "Noeud de départ chemin " + (i + 1));
            assertEquals(ordreAttendu[i + 1], arriveeId, "Noeud d'arrivée chemin " + (i + 1));

            assertEquals(distancesAttendu[i], c.getLongueurTotal(), 1.0, "Distance chemin " + (i + 1));

            LocalTime departHoraire = c.getNoeudDePassageDepart().getHoraireDepart();
            if (departHoraire == null) departHoraire = c.getNoeudDePassageDepart().getHoraireArrivee();
            assertEquals(LocalTime.parse(horairesDepartAttendu[i]), departHoraire, "Horaire départ chemin " + (i + 1));

            assertEquals(LocalTime.parse(horairesArriveeAttendu[i]), c.getNoeudDePassageArrivee().getHoraireArrivee(), "Horaire arrivée chemin " + (i + 1));
        }
    }


}
