package fr.insalyon.pldagile.modele;

import fr.insalyon.pldagile.modele.NoeudDePassage.TypeNoeud;
import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DemandeDeLivraisonParseurXML {
    public static DemandeDeLivraison loadFromFile(File fichierXML, Carte carte) throws Exception {
        if (fichierXML != null && fichierXML.exists() && fichierXML.isFile()) {
            if (carte == null) {
                throw new IllegalArgumentException("Carte ne peut pas être null !");
            } else {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(fichierXML);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                if (!"demandeDeLivraisons".equals(root.getTagName())) {
                    throw new IllegalArgumentException("Racine XML invalide : " + root.getTagName());
                } else {
                    NodeList entrepots = root.getElementsByTagName("entrepot");
                    if (entrepots.getLength() != 1) {
                        throw new IllegalArgumentException("Il doit y avoir exactement un entrepot.");
                    } else {
                        Element eEntrepot = (Element)entrepots.item(0);
                        long idEntrepot = Long.parseLong(eEntrepot.getAttribute("adresse"));
                        LocalTime heureDepart = parseHeure(eEntrepot.getAttribute("heureDepart"));
                        NoeudDePassage entrepot = new NoeudDePassage(idEntrepot, ((Noeud)carte.getNoeuds().get(idEntrepot)).getLatitude(), ((Noeud)carte.getNoeuds().get(idEntrepot)).getLongitude(), TypeNoeud.ENTREPOT, (double)0.0F, heureDepart);
                        NodeList livraisonsXML = root.getElementsByTagName("livraison");
                        List<Livraison> livraisons = new ArrayList();

                        for(int i = 0; i < livraisonsXML.getLength(); ++i) {
                            Element eLivraison = (Element)livraisonsXML.item(i);
                            long idEnlevement = Long.parseLong(eLivraison.getAttribute("adresseEnlevement"));
                            long idLivraison = Long.parseLong(eLivraison.getAttribute("adresseLivraison"));
                            double dureeEnlevement = Double.parseDouble(eLivraison.getAttribute("dureeEnlevement"));
                            double dureeLivraison = Double.parseDouble(eLivraison.getAttribute("dureeLivraison"));
                            Noeud enlevementNoeud = (Noeud)carte.getNoeuds().get(idEnlevement);
                            Noeud livraisonNoeud = (Noeud)carte.getNoeuds().get(idLivraison);
                            if (enlevementNoeud == null || livraisonNoeud == null) {
                                throw new IllegalArgumentException("Noeud inexistant pour livraison : " + idEnlevement + " ou " + idLivraison);
                            }

                            NoeudDePassage enlevement = new NoeudDePassage(idEnlevement, enlevementNoeud.getLatitude(), enlevementNoeud.getLongitude(), TypeNoeud.PICKUP, dureeEnlevement, (LocalTime)null);
                            NoeudDePassage livraisonNP = new NoeudDePassage(idLivraison, livraisonNoeud.getLatitude(), livraisonNoeud.getLongitude(), TypeNoeud.DELIVERY, dureeLivraison, (LocalTime)null);
                            livraisons.add(new Livraison(enlevement, livraisonNP));
                        }

                        return new DemandeDeLivraison(entrepot, livraisons);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Fichier XML invalide ou introuvable !");
        }
    }

    private static LocalTime parseHeure(String h) {
        String[] parts = h.split(":");
        int heure = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int seconde = Integer.parseInt(parts[2]);
        return LocalTime.of(heure, minute, seconde);
    }
}