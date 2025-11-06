package fr.insalyon.pldagile.modele;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;

/**
 * Parseur pour charger une {@link Carte} depuis un fichier XML,
 * avec option de validation via un fichier XSD.
 */
public class CarteParseurXML {

    /**
     * Charge une carte depuis un fichier XML et valide le format si un fichier XSD est fourni.
     *
     * @param fichierXML Le fichier XML à lire.
     * @param xsdFile    Le fichier XSD pour validation (optionnel, peut être null).
     * @return La carte construite à partir du XML.
     * @throws Exception Si le fichier est invalide ou si une erreur survient lors du parsing.
     */
    public static Carte loadFromFile(File fichierXML, File xsdFile) throws Exception {
        if (fichierXML == null || !fichierXML.exists() || !fichierXML.isFile()) {
            throw new IllegalArgumentException("Fichier XML invalide ou introuvable !");
        }

        Carte carte = new Carte();

        try {
            if (xsdFile != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                Schema schema = schemaFactory.newSchema(xsdFile);
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(fichierXML));
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc;
            try {
                doc = builder.parse(fichierXML);
            } catch (SAXException e) {
                throw new XMLFormatException("Erreur de formatage XML : document mal formé (" + e.getMessage() + ")", e);
            }

            doc.getDocumentElement().normalize();

            Element racine = doc.getDocumentElement();
            if (racine == null || !"reseau".equals(racine.getNodeName())) {
                throw new XMLFormatException("Erreur formatage XML : la balise racine <reseau> est manquante ou incorrecte.");
            }

            NodeList enfants = racine.getChildNodes();
            for (int i = 0; i < enfants.getLength(); i++) {
                if (enfants.item(i) instanceof Element e) {
                    String nom = e.getNodeName();
                    if (!nom.equals("noeud") && !nom.equals("troncon")) {
                        throw new XMLFormatException("Erreur formatage XML : balise inconnue <" + nom + "> détectée.");
                    }
                }
            }

            NodeList noeuds = doc.getElementsByTagName("noeud");
            for (int i = 0; i < noeuds.getLength(); ++i) {
                Element e = (Element) noeuds.item(i);
                String idAttr = e.getAttribute("id");
                String latAttr = e.getAttribute("latitude");
                String lonAttr = e.getAttribute("longitude");

                if (idAttr.isEmpty() || latAttr.isEmpty() || lonAttr.isEmpty()) {
                    throw new XMLFormatException(
                            "Erreur formatage XML : attribut(s) manquant(s) pour le noeud #" + i +
                                    " (id='" + idAttr + "', latitude='" + latAttr + "', longitude='" + lonAttr + "')"
                    );
                }

                try {
                    long id = Long.parseLong(idAttr);
                    double latitude = Double.parseDouble(latAttr);
                    double longitude = Double.parseDouble(lonAttr);

                    if (carte.getNoeuds().containsKey(id)) {
                        throw new XMLFormatException(
                                "Erreur formatage XML : contrainte UNIQUE violée - un noeud avec l'id '" + id + "' existe déjà."
                        );
                    }

                    carte.AjouterNoeud(new Noeud(id, latitude, longitude));
                } catch (NumberFormatException ex) {
                    throw new XMLFormatException(
                            "Erreur formatage XML : format invalide pour le noeud #" + i +
                                    " (id='" + idAttr + "', latitude='" + latAttr + "', longitude='" + lonAttr + "')",
                            ex
                    );
                }
            }

            NodeList troncons = doc.getElementsByTagName("troncon");
            for (int i = 0; i < troncons.getLength(); ++i) {
                Element e = (Element) troncons.item(i);
                String origineAttr = e.getAttribute("origine");
                String destinationAttr = e.getAttribute("destination");
                String longueurAttr = e.getAttribute("longueur");
                String nomRueAttr = e.getAttribute("nomRue");

                if (origineAttr.isEmpty() || destinationAttr.isEmpty() || longueurAttr.isEmpty()) {
                    throw new XMLFormatException(
                            "Erreur formatage XML : attribut(s) manquant(s) pour le tronçon #" + i +
                                    " (origine='" + origineAttr + "', destination='" + destinationAttr + "', longueur='" + longueurAttr + "', nomRue='" + nomRueAttr + "')"
                    );
                }

                try {
                    long origine = Long.parseLong(origineAttr);
                    long destination = Long.parseLong(destinationAttr);
                    double longueur = Double.parseDouble(longueurAttr);

                    if (!carte.getNoeuds().containsKey(origine) || !carte.getNoeuds().containsKey(destination)) {
                        throw new XMLFormatException(
                                "Erreur formatage XML : contrainte FOREIGN KEY non respectée - les noeuds avec l'id '" + origine + "' ou '" + destination + "' n'existent pas."
                        );
                    }

                    carte.AjouterTroncon(new Troncon(origine, destination, longueur, nomRueAttr));
                } catch (NumberFormatException ex) {
                    throw new XMLFormatException(
                            "Erreur formatage XML : format invalide pour le tronçon #" + i +
                                    " (origine='" + origineAttr + "', destination='" + destinationAttr + "', longueur='" + longueurAttr + "', nomRue='" + nomRueAttr + "')",
                            ex
                    );
                }
            }

            return carte;

        } catch (XMLFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLFormatException("Erreur lors de la lecture du fichier XML : " + e.getMessage(), e);
        }
    }

    /**
     * Charge une carte depuis un fichier XML sans validation XSD.
     *
     * @param fichierXML Le fichier XML à lire.
     * @return La carte construite à partir du XML.
     * @throws Exception Si le fichier est invalide ou si une erreur survient lors du parsing.
     */
    public static Carte loadFromFile(File fichierXML) throws Exception {
        return loadFromFile(fichierXML, null);
    }
}
