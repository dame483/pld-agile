/*package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.Chemin;
import fr.insalyon.pldagile.modele.NoeudDePassage;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.modele.Troncon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class parseurTourneeJson {
    public parseurTourneeJson() {
    }
    public static Tournee parseurTournee(File sauvegardeTournee) throws Exception {
        try {

        String chemin = "src/main/java/fr/insalyon/pldagile/sortie/sauvegardeTournee.json";
        System.out.println("Lecture et parsing de: " + chemin);
        String contenu = new String(Files.readAllBytes(Paths.get(chemin)));
        JSONObject tournee = new JSONObject(contenu);
        JSONArray cheminsArray = tournee.getJSONArray("tournee");
        List<Troncon> listTroncons = null;
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
            NoeudDePassage noeudDePassageDepart = noeudDePassageDepartJson.getString("id");



            }
            JSONObject longueurTotale = cheminsArray;

        }

        JSONObject cheminObject = new JSONObject();
        Troncon troncon = new Troncon(idDepart, idArrivee, longueur, nomRue);
        listTroncon.add(troncon);
        Chemin chemin = new Chemin(listTroncon, longueurTotale, )

        }
    }
}
*/