package fr.insalyon.pldagile;

import fr.insalyon.pldagile.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DemandeDeLivraisonTests {

    private Carte carte;

    @BeforeEach
    void setUp() {
        carte = new Carte();
        carte.AjouterNoeud(new Noeud(1L, 45.75, 4.85));
        carte.AjouterNoeud(new Noeud(2L, 45.76, 4.86));
        carte.AjouterNoeud(new Noeud(3L, 45.77, 4.87));
        carte.AjouterNoeud(new Noeud(4L, 45.78, 4.88));
        carte.AjouterNoeud(new Noeud(5L, 45.79, 4.89));
    }

    private File createTempXML(String content) throws Exception {
        File temp = File.createTempFile("demande", ".xml");
        temp.deleteOnExit();
        try (FileWriter writer = new FileWriter(temp)) {
            writer.write(content);
        }
        return temp;
    }

    @Test
    void testLoadFromFile_validFile_shouldParseSuccessfully() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="08:30:00" />
                <livraison adresseEnlevement="2" adresseLivraison="3"
                           dureeEnlevement="120" dureeLivraison="180" />
            </demandeDeLivraisons>
        """;

        File fichier = createTempXML(xml);
        DemandeDeLivraison ddl = DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte);

        assertNotNull(ddl);
        assertNotNull(ddl.getEntrepot());
        assertEquals(1L, ddl.getEntrepot().getId());
        assertEquals(LocalTime.of(8, 30), ddl.getEntrepot().getHoraireDepart());
        assertNull(ddl.getEntrepot().getHoraireArrivee());
        assertEquals(1, ddl.getLivraisons().size());
        Livraison liv = ddl.getLivraisons().get(0);
        assertEquals(2L, liv.getAdresseEnlevement().getId());
        assertEquals(3L, liv.getAdresseLivraison().getId());
        assertEquals(120.0, liv.getAdresseEnlevement().getDuree(), 0.001);
        assertEquals(180.0, liv.getAdresseLivraison().getDuree(), 0.001);
    }

    @Test
    void testLoadFromFile_multipleLivraisons_shouldParseAll() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="09:00:00" />
                <livraison adresseEnlevement="2" adresseLivraison="3"
                           dureeEnlevement="60" dureeLivraison="90" />
                <livraison adresseEnlevement="4" adresseLivraison="5"
                           dureeEnlevement="45" dureeLivraison="75" />
            </demandeDeLivraisons>
        """;

        File fichier = createTempXML(xml);
        DemandeDeLivraison ddl = DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte);

        assertEquals(2, ddl.getLivraisons().size());
        assertEquals(2L, ddl.getLivraisons().get(0).getAdresseEnlevement().getId());
        assertEquals(4L, ddl.getLivraisons().get(1).getAdresseEnlevement().getId());
        assertEquals(LocalTime.of(9, 0), ddl.getEntrepot().getHoraireDepart());
    }

    @Test
    void testLoadFromFile_missingEntrepot_shouldThrowException() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <livraison adresseEnlevement="2" adresseLivraison="3"
                           dureeEnlevement="120" dureeLivraison="180" />
            </demandeDeLivraisons>
        """;

        File fichier = createTempXML(xml);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte));
        assertTrue(ex.getMessage().toLowerCase().contains("entrepot"));
    }

    @Test
    void testLoadFromFile_invalidRoot_shouldThrowException() throws Exception {
        String xml = """
            <autreRacine>
                <entrepot adresse="1" heureDepart="08:00:00" />
            </autreRacine>
        """;

        File fichier = createTempXML(xml);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte));
        assertTrue(ex.getMessage().toLowerCase().contains("racine xml invalide"));
    }

    @Test
    void testLoadFromFile_missingNodeInCarte_shouldThrowException() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="08:00:00" />
                <livraison adresseEnlevement="999" adresseLivraison="3"
                           dureeEnlevement="60" dureeLivraison="120" />
            </demandeDeLivraisons>
        """;

        File fichier = createTempXML(xml);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte));
        assertTrue(ex.getMessage().toLowerCase().contains("noeud inexistant"));
    }

    @Test
    void testLoadFromFile_nullFile_shouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(null, carte));
        assertTrue(ex.getMessage().toLowerCase().contains("fichier xml invalide"));
    }

    @Test
    void testLoadFromFile_nullCarte_shouldThrowException() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="08:00:00" />
            </demandeDeLivraisons>
        """;

        File fichier = createTempXML(xml);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(fichier, null));
        assertTrue(ex.getMessage().toLowerCase().contains("carte ne peut pas être null"));
    }

    @Test
    void testLoadFromFile_malformedXML_shouldThrowException() throws Exception {
        String xml = """
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="08:00:00">
                <livraison adresseEnlevement="2" adresseLivraison="3" dureeEnlevement="60" dureeLivraison="120" />
            </demandeDeLivraisons
        """;

        File fichier = createTempXML(xml);
        assertThrows(Exception.class, () ->
                DemandeDeLivraisonParseurXML.loadFromFile(fichier, carte));
    }
}