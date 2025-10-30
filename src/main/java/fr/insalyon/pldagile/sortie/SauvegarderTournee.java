package fr.insalyon.pldagile.sortie;

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

    public SauvegarderTournee(List<Tournee> listTournee) {
        this.listTournee = listTournee;
    }

    public void sauvegarderTournee() throws Exception {
        for (int i = 0; i < listTournee.size(); i++) {
            List<Chemin> chemins = listTournee.get(i).getChemins();
            JSONArray cheminsArray = new JSONArray();
            System.out.println("appel de sauvegarde tournée");
            try {
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
                File jsonFile = new File("src/main/java/fr/insalyon/pldagile/sortie/tourneeJson/sauvegardeTourne" + (i + 1) + ".json");

                try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                    JSONObject tourneeObject = new JSONObject();
                    tourneeObject.put("tournee", cheminsArray);
                    fileWriter.write(tourneeObject.toString(4));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("Erreur lors de la sauvegarde de la tournée", e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
