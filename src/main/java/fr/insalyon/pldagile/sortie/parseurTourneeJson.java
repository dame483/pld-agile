package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.Chemin;
import fr.insalyon.pldagile.modele.NoeudDePassage;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.modele.Troncon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class parseurTourneeJson {
    public parseurTourneeJson() {
    }

    public static Tournee parseurTournee(String cheminFichier) throws Exception {
        Tournee tourneeDuLivreur = null;
        LocalTime heureDepart = null;
        LocalTime heureArriveeFinale = null;
        try {

            System.out.println("Lecture et parsing de: " + cheminFichier);
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier)));
            JSONObject tournee = new JSONObject(contenu);
            JSONArray cheminsArray = tournee.getJSONArray("tournee");
            List<Chemin> listChemins = new ArrayList<>();
            double dureeTotale = 0.0;
            for (int i = 0; i < cheminsArray.length(); i++) {
                List<Troncon> listTroncons = new ArrayList<>();
                JSONObject cheminObject = cheminsArray.getJSONObject(i);
                JSONArray tronconArray = cheminObject.getJSONArray("troncons");
                double longueurTotale = cheminObject.getDouble("longueurTotale");
                JSONObject noeudDePassageDepartJson = cheminObject.getJSONObject("NoeudDePassageDepart");
                JSONObject noeudDePassageArriveeJson = cheminObject.getJSONObject("NoeudDePassageArrivee");
                for (int j = 0; j < tronconArray.length(); j++) {
                    JSONObject tronconObject = tronconArray.getJSONObject(j);
                    Troncon troncon = new Troncon(tronconObject.getLong("idOrigine"),
                            tronconObject.getLong("idDestination"),
                            tronconObject.getDouble("longueur"),
                            tronconObject.getString("nomRue"));
                    listTroncons.add(troncon);
                }
                long idDepart = (long) noeudDePassageDepartJson.getInt("id");
                double latitudeDepart = (double) noeudDePassageDepartJson.getInt("latitude");
                double longitudeDepart = (double) noeudDePassageDepartJson.getInt("longitude");
                NoeudDePassage.TypeNoeud typeNoeudDepart = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageDepartJson.getString("typeNoeud"));
                LocalTime horaireDepartDepart = LocalTime.parse(noeudDePassageDepartJson.getString("horaireDepart"));
                LocalTime horaireArriveeDepart = LocalTime.parse(noeudDePassageDepartJson.getString("horaireArrivee"));

                if(noeudDePassageDepartJson.getString("typeNoeud").equalsIgnoreCase("ENTREPOT")) {
                    heureDepart = horaireDepartDepart; //récupération de l'horaire de départ
                }

                NoeudDePassage noeudDePassageDepart = new NoeudDePassage(idDepart, latitudeDepart, longitudeDepart, typeNoeudDepart, 0.0, horaireArriveeDepart);
                noeudDePassageDepart.setHoraireDepart(horaireDepartDepart);

                long idArrivee = (long) noeudDePassageArriveeJson.getInt("id");
                double latitudeArrivee = (double) noeudDePassageArriveeJson.getInt("latitude");
                double longitudeArrivee = (double) noeudDePassageArriveeJson.getInt("longitude");
                NoeudDePassage.TypeNoeud typeNoeudArrivee = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageArriveeJson.getString("typeNoeud"));
                LocalTime horaireDepartArrivee = LocalTime.parse(noeudDePassageArriveeJson.getString("horaireDepart"));
                LocalTime horaireArriveeArrivee = LocalTime.parse(noeudDePassageArriveeJson.getString("horaireArrivee"));

                if(noeudDePassageArriveeJson.getString("typeNoeud").equalsIgnoreCase("ENTREPOT")) {
                    heureArriveeFinale = horaireArriveeArrivee; //récupération de l'horaire de départ
                }
                NoeudDePassage noeudDePassageArrivee = new NoeudDePassage(idArrivee, latitudeArrivee, longitudeArrivee, typeNoeudArrivee, 0.0, horaireArriveeArrivee);
                noeudDePassageArrivee.setHoraireDepart(horaireDepartArrivee);
                Chemin chemin = new Chemin(listTroncons, longueurTotale, noeudDePassageDepart, noeudDePassageArrivee);
                listChemins.add(chemin);

            }

            if (heureDepart != null && heureArriveeFinale != null) {
                dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);
            }

            else {
                System.err.println("Heure Départ ou heure Arrivée finale manquante pour l'entrepôt  ");
            }

            double longueurTotale = listChemins.stream().mapToDouble(Chemin::getLongueurTotal).sum();
            tourneeDuLivreur = new Tournee(listChemins, dureeTotale, longueurTotale);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return tourneeDuLivreur;
    }
}