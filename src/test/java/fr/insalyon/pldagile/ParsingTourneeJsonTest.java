package fr.insalyon.pldagile;

import fr.insalyon.pldagile.algorithme.CalculTournees;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.FeuilleDeRoute;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParsingTourneeJsonTest {

    @Test
    void testTourneeParseeEgaleInitiale() throws Exception {
        File fichierCarte = new File("src/main/resources/donnees/plans/moyenPlan.xml");
        Carte ville = CarteParseurXML.loadFromFile(fichierCarte);
        System.out.println("Carte chargée.");

        //  Charger la demande de livraison
        File fichierDemande = new File("src/main/resources/donnees/demandes/demandeMoyen5.xml");
        DemandeDeLivraison demande = DemandeDeLivraisonParseurXML.loadFromFile(fichierDemande, ville);
        System.out.println("Demande de livraison chargée.");

        //  Paramètres de la tournée : heure de départ depuis le parseur ou défaut 08:00
        LocalTime heureDepart;
        if (demande.getEntrepot() != null && demande.getEntrepot().getHoraireDepart() != null) {
            heureDepart = demande.getEntrepot().getHoraireDepart();
        } else {
            heureDepart = LocalTime.of(8, 0);
        }

        double vitesse = 4.16; // m/s

        //  Calcul de la tournée
        CalculTournees calculTournee = new CalculTournees(ville, demande, vitesse, 1, heureDepart); // 1 livreur pour le test
        System.out.println("\nCalcul de la tournée initiale");
        Tournee tournee = calculTournee.calculerTournees().get(0);

        //Parsing de la tournée
        FeuilleDeRoute feuilleDeRoute = new FeuilleDeRoute(tournee);
        feuilleDeRoute.sauvegarderTournee();
        parseurTourneeJson parseurJson = new parseurTourneeJson();
        Tournee tourneeParseur = parseurJson.parseurTournee("src/main/java/fr/insalyon/pldagile/sortie/sauvegardeTourne.json");


        assertEquals(tournee.getChemins().size(), tourneeParseur.getChemins().size(), "La tournée parsée doit etre identique que la tournée initiale en chemins");
        assertEquals(tournee.getDureeTotale(), tourneeParseur.getDureeTotale(),"les tournées doivent etre identiques en terme de durée totale");
        assertEquals(tournee.getDistanceTotale(), tourneeParseur.getDistanceTotale(), "les distances totales doivernt etre identiques");

    }

}
