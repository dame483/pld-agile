package fr.insalyon.pldagile;
import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.modele.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AjoutCheminTourneeTests {
    @Test
    void testGetNoeudParId() throws Exception {
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
    void testAjoutLivraison() throws Exception {
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
        // Vérifier que le noeud à ajouter n'existe pas dans la tournée
        long idNoeudAAjouter = 1957527553;
        long idNoeudPrecedent = 208769457;
        NoeudDePassage noeudPrecedent = tournee.getNoeudParId(idNoeudPrecedent);
        NoeudDePassage n = tournee.getNoeudParId(idNoeudAAjouter);
        assertNull(n, "Le noeud à ajouter ne doit pas exister dans la tournée");
        assertNotNull(noeudPrecedent, "Le noeud precedent le noeud à ajouter ne doit pas être null ");

        CalculChemins calculChemins = new CalculChemins(ville);
        ModificationTournee modificationTournee = new ModificationTournee(calculChemins, vitesse);
        // ajouter le noeud
        double dureeEnlevement = 300.0;
        Tournee tourneeModifiee = modificationTournee.ajouterNoeudPickup(tournee, idNoeudAAjouter, idNoeudPrecedent,dureeEnlevement);
        assertNotEquals(tourneeModifiee, tournee, "La tournée doit etre modifiée");



    }

}
