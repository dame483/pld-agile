package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.Chemin;
import fr.insalyon.pldagile.modele.NoeudDePassage;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.modele.Troncon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

public class parseurTourneeJson {
    public parseurTourneeJson() {
    }

    public static Tournee parseurTournee(File sauvegardeTournee) throws Exception {
        try {

            String path = "src/main/java/fr/insalyon/pldagile/sortie/sauvegardeTournee.json";
            System.out.println("Lecture et parsing de: " + path);
            String contenu = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject tournee = new JSONObject(contenu);
            JSONArray cheminsArray = tournee.getJSONArray("tournee");
            List<Troncon> listTroncons = null;
            List<Chemin> listChemins = null;
            Tournee tourneeDuLivreur = null;
            for (int i = 0; i < cheminsArray.length(); i++) {
                JSONObject cheminObject = cheminsArray.getJSONObject(i);
                JSONArray tronconArray = cheminObject.getJSONArray("troncons");
                double longueurTotale = cheminObject.getInt("longueurTotale");
                JSONObject noeudDePassageDepartJson = cheminObject.getJSONObject("NoeudDePassageDepart");
                JSONObject noeudDePassageArriveeJson = cheminObject.getJSONObject("NoeudDePassageArrivee");
                for (int j = 0; j < tronconArray.length(); j++) {
                    JSONObject tronconObject = tronconArray.getJSONObject(j);
                    Troncon troncon = new Troncon(tronconObject.getInt("idOrigine"),
                            tronconObject.getInt("idDestination"),
                            tronconObject.getInt("longueur"),
                            tronconObject.getString("nomRue"));
                    listTroncons.add(troncon);
                }
                long idDepart = (long) noeudDePassageDepartJson.getInt("id");
                double latitudeDepart = (double) noeudDePassageDepartJson.getInt("latitude");
                double longitudeDepart = (double) noeudDePassageDepartJson.getInt("longitude");
                NoeudDePassage.TypeNoeud typeNoeudDepart = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageDepartJson.getString("TypeNoeud"));
                LocalTime horaireArriveeDepart = LocalTime.parse(noeudDePassageDepartJson.getString("horaireArrivee"));

                NoeudDePassage noeudDePassageDepart = new NoeudDePassage(idDepart, latitudeDepart, longitudeDepart, typeNoeudDepart, 0.0, horaireArriveeDepart);

                long idArrivee = (long) noeudDePassageDepartJson.getInt("id");
                double latitudeArrivee = (double) noeudDePassageDepartJson.getInt("latitude");
                double longitudeArrivee = (double) noeudDePassageDepartJson.getInt("longitude");
                NoeudDePassage.TypeNoeud typeNoeudArrivee = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageDepartJson.getString("TypeNoeud"));
                LocalTime horaireArriveeArrivee = LocalTime.parse(noeudDePassageDepartJson.getString("horaireArrivee"));

                NoeudDePassage noeudDePassageArrivee = new NoeudDePassage(idArrivee, latitudeArrivee, longitudeArrivee, typeNoeudArrivee, 0.0, horaireArriveeArrivee);

                Chemin chemin = new Chemin(listTroncons, longueurTotale, noeudDePassageDepart, noeudDePassageArrivee);
                listChemins.add(chemin);
            }

            tourneeDuLivreur = new Tournee(listChemins, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tourneeDuLivreur;
    }
}