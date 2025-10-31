package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.Chemin;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.modele.Troncon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class SauvegarderTournee {
    private List<Tournee> listTournee;
    private Carte carte;

    public SauvegarderTournee(List<Tournee> listTournee, Carte carte) {
        this.listTournee = listTournee;
        this.carte = carte;
    }

    public void sauvegarderTournee() throws Exception {
        File jsonFile = new File("src/main/java/fr/insalyon/pldagile/sortie/tourneeJson/sauvegardeTourne.json");
        JSONArray tourneesArray = new JSONArray();

        try {
            for (Tournee tournee : listTournee) {
                List<Chemin> chemins = tournee.getChemins();
                JSONObject tourneeObject = new JSONObject();
                JSONArray cheminsArray = new JSONArray();
                System.out.println("appel de sauvegarde tournée");
                for (Chemin chemin : chemins) {
                    JSONObject cheminObject = new JSONObject();

                    JSONArray tronconsArray = new JSONArray();
                    for (Troncon troncon : chemin.getTroncons()) {
                        JSONObject tronconObject = new JSONObject();
                        tronconObject.put("idOrigine", troncon.getIdOrigine());
                        tronconObject.put("idDestination", troncon.getIdDestination());
                        tronconObject.put("longueur", troncon.getLongueur());
                        tronconObject.put("nomRue", troncon.getNomRue());
                        tronconsArray.put(tronconObject);
                    }

                    cheminObject.put("troncons", tronconsArray);
                    cheminObject.put("longueurTotale", chemin.getLongueurTotal());

                    JSONObject noeuddePassageDepart = new JSONObject();
                    noeuddePassageDepart.put("id", chemin.getNoeudDePassageDepart().getId());
                    noeuddePassageDepart.put("latitude", chemin.getNoeudDePassageDepart().getLatitude());
                    noeuddePassageDepart.put("longitude", chemin.getNoeudDePassageDepart().getLongitude());
                    noeuddePassageDepart.put("typeNoeud", chemin.getNoeudDePassageDepart().getType());
                    noeuddePassageDepart.put("horaireArrivee", chemin.getNoeudDePassageDepart().getHoraireArrivee().toString());
                    noeuddePassageDepart.put("horaireDepart", chemin.getNoeudDePassageDepart().getHoraireDepart().toString());
                    cheminObject.put("NoeudDePassageDepart", noeuddePassageDepart);

                    JSONObject noeudDePassageArrivee = new JSONObject();
                    noeudDePassageArrivee.put("id", chemin.getNoeudDePassageArrivee().getId());
                    noeudDePassageArrivee.put("latitude", chemin.getNoeudDePassageArrivee().getLatitude());
                    noeudDePassageArrivee.put("longitude", chemin.getNoeudDePassageArrivee().getLongitude());
                    noeudDePassageArrivee.put("typeNoeud", chemin.getNoeudDePassageArrivee().getType());
                    noeudDePassageArrivee.put("horaireArrivee", chemin.getNoeudDePassageArrivee().getHoraireArrivee().toString());
                    noeudDePassageArrivee.put("horaireDepart", chemin.getNoeudDePassageArrivee().getHoraireDepart().toString());
                    cheminObject.put("NoeudDePassageArrivee", noeudDePassageArrivee);

                    cheminsArray.put(cheminObject);
                }
                tourneeObject.put("dureeTotale", tournee.getDureeTotale());
                tourneeObject.put("chemins", cheminsArray);
                tourneesArray.put(tourneeObject);
            }

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                JSONObject root = new JSONObject();
                root.put("Carte", carte);
                root.put("tournees", tourneesArray);
                fileWriter.write(root.toString(4));
                System.out.println("Sauvegarde terminée : " + jsonFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Erreur lors de la sauvegarde de la tournée", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la sauvegarde de la tournée", e);
        }
    }
}