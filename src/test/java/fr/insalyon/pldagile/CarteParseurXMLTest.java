package fr.insalyon.pldagile;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.Noeud;
import fr.insalyon.pldagile.modele.Troncon;
import fr.insalyon.pldagile.exception.XMLFormatException;


import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class CarteParseurXMLTest {

    private File createTempXmlFile(String contenu) throws IOException {
        File tempFile = File.createTempFile("test", ".xml");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(contenu);
        }
        return tempFile;
    }

    @Test
    void testParsingPlan() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                <noeud id="1" latitude="45.781198" longitude="4.871840"/>
                <noeud id="2" latitude="45.782430" longitude="4.877183"/>
                <noeud id="3" latitude="45.782241" longitude="4.877250"/>
                <noeud id="4" latitude="45.779502" longitude="4.875016"/>
                <noeud id="5" latitude="45.779064" longitude="4.872468"/>
                <troncon destination="2" longueur="430" nomRue="Avenue Jean Capelle" origine="1"/>
                <troncon destination="1" longueur="430" nomRue="Avenue Jean Capelle" origine="2"/>
                <troncon destination="5" longueur="240" nomRue="Avenue Gaston Berger" origine="1"/>
                <troncon destination="1" longueur="240" nomRue="Avenue Gaston Berger" origine="5"/>
                <troncon destination="5" longueur="190" nomRue="Boulevard du 11 novembre 1918" origine="4"/>
                <troncon destination="4" longueur="190" nomRue="Boulevard du 11 novembre 1918" origine="5"/>
                <troncon destination="4" longueur="360" nomRue="Avenue Albert Einstein" origine="3"/>
                <troncon destination="3" longueur="360" nomRue="Avenue Albert Einstein" origine="4"/>
                <troncon destination="3" longueur="20.2" nomRue="" origine="2"/>
                <troncon destination="2" longueur="20.2" nomRue="" origine="3"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);
        Carte carte = CarteParseurXML.loadFromFile(fichier);
        assertEquals(5, carte.getNoeuds().size());
        assertEquals(10, carte.getTroncons().size());
    }

    @Test
    void testNoeudSansLongitude() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"25175791\" latitude=\"45.75406\" longitude=\"\"/>" +
                "<noeud id=\"2129259178\" latitude=\"45.750404\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudSansBaliseLongitude() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"25175791\" latitude=\"45.75406\"/>" +
                "<noeud id=\"2129259178\" latitude=\"45.750404\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudSansLatitude() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"25175791\" latitude=\"45.75406\" longitude=\"4.857418\"/>" +
                "<noeud id=\"2129259178\" latitude=\"\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudSansBaliseLatitude() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"25175791\" latitude=\"45.75406\" longitude=\"4.857418\"/>" +
                "<noeud id=\"2129259178\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudSansBaliseId() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud latitude=\"45.75406\" longitude=\"4.857418\"/>" +
                "<noeud id=\"2129259178\" latitude=\"45.750404\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudSansId() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"\" latitude=\"45.75406\" longitude=\"4.857418\"/>" +
                "<noeud id=\"2129259178\" latitude=\"45.750404\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testNoeudsIdIdentiques() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<reseau><noeud id=\"25175791\" latitude=\"45.75406\" longitude=\"4.857418\"/>" +
                "<noeud id=\"25175791\" latitude=\"45.750404\" longitude=\"4.8744674\"/></reseau>";
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }
    @Test
    void testTronconSansLongueur() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon origine="1" destination="2" longueur="" nomRue="Rue Test"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconSansBaliseLongueur() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon origine="1" destination="2" nomRue="Rue Test"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconSansOrigine() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon origine="" destination="2" longueur="120.5" nomRue="Rue Test"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconSansBaliseOrigine() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon destination="2" longueur="120.5" nomRue="Rue Test"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconSansDestination() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon destination="" longueur="120.5" nomRue="Rue Test" origine="1"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconSansBaliseDestination() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon longueur="120.5" nomRue="Rue Test" origine="1"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconLongueurInvalide() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon destination="2" longueur="abc" nomRue="Rue Test" origine="1"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }

    @Test
    void testTronconNoeudsInexistants() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <reseau>
                    <noeud id="1" latitude="45.75" longitude="4.85"/>
                    <noeud id="2" latitude="45.76" longitude="4.86"/>
                    <troncon destination="4" longueur="121" nomRue="Rue Test" origine="3"/>
                </reseau>
                """;
        File fichier = createTempXmlFile(xml);

        assertThrows(XMLFormatException.class, () -> {
            CarteParseurXML.loadFromFile(fichier);
        });
    }




}