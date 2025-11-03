package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class parseurTourneeJson {
    public parseurTourneeJson() {
    }

    public static List<Tournee> parseurTournee(String cheminFichier) throws Exception {
        List<Tournee> listTournee = new ArrayList<>();
        LocalTime heureDepart = null;
        LocalTime heureArriveeFinale = null;
        try {

            System.out.println("Lecture et parsing de: " + cheminFichier);
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier)));
            JSONObject tournee = new JSONObject(contenu);
            JSONArray tournees = tournee.getJSONArray("tournees");
            for (int k = 0; k < tournees.length(); k++) {
                JSONObject tourneeObject = tournees.getJSONObject(k);
                JSONArray cheminsArray = tourneeObject.getJSONArray("chemins");

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
                    long idDepart =  noeudDePassageDepartJson.getLong("id");
                    double latitudeDepart =  noeudDePassageDepartJson.getDouble("latitude");
                    double longitudeDepart =  noeudDePassageDepartJson.getDouble("longitude");
                    NoeudDePassage.TypeNoeud typeNoeudDepart = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageDepartJson.getString("typeNoeud"));
                    LocalTime horaireDepartDepart = LocalTime.parse(noeudDePassageDepartJson.getString("horaireDepart"));
                    LocalTime horaireArriveeDepart = LocalTime.parse(noeudDePassageDepartJson.getString("horaireArrivee"));
                    double dureeDepart =  noeudDePassageDepartJson.getDouble("duree");
                    if (noeudDePassageDepartJson.getString("typeNoeud").equalsIgnoreCase("ENTREPOT")) {
                        heureDepart = horaireDepartDepart; //récupération de l'horaire de départ
                    }

                    NoeudDePassage noeudDePassageDepart = new NoeudDePassage(idDepart, latitudeDepart, longitudeDepart, typeNoeudDepart, dureeDepart, horaireArriveeDepart);
                    noeudDePassageDepart.setHoraireDepart(horaireDepartDepart);

                    long idArrivee =  noeudDePassageArriveeJson.getLong("id");
                    double latitudeArrivee =  noeudDePassageArriveeJson.getDouble("latitude");
                    double longitudeArrivee =  noeudDePassageArriveeJson.getDouble("longitude");
                    double dureeArrivee =  noeudDePassageArriveeJson.getDouble("duree");
                    NoeudDePassage.TypeNoeud typeNoeudArrivee = NoeudDePassage.TypeNoeud.valueOf(noeudDePassageArriveeJson.getString("typeNoeud"));
                    LocalTime horaireDepartArrivee = LocalTime.parse(noeudDePassageArriveeJson.getString("horaireDepart"));
                    LocalTime horaireArriveeArrivee = LocalTime.parse(noeudDePassageArriveeJson.getString("horaireArrivee"));

                    if (noeudDePassageArriveeJson.getString("typeNoeud").equalsIgnoreCase("ENTREPOT")) {
                        heureArriveeFinale = horaireArriveeArrivee;
                    }
                    NoeudDePassage noeudDePassageArrivee = new NoeudDePassage(idArrivee, latitudeArrivee, longitudeArrivee, typeNoeudArrivee, dureeArrivee, horaireArriveeArrivee);
                    noeudDePassageArrivee.setHoraireDepart(horaireDepartArrivee);
                    Chemin chemin = new Chemin(listTroncons, longueurTotale, noeudDePassageDepart, noeudDePassageArrivee);
                    listChemins.add(chemin);

                }

                if (heureDepart != null && heureArriveeFinale != null) {
                    dureeTotale = ChronoUnit.SECONDS.between(heureDepart, heureArriveeFinale);
                } else {
                    System.err.println("Heure Départ ou heure Arrivée finale manquante pour l'entrepôt  ");
                }

                double longueurTotale = listChemins.stream().mapToDouble(Chemin::getLongueurTotal).sum();
                Tournee tourneeDuLivreur = new Tournee(listChemins, dureeTotale, longueurTotale);
                listTournee.add(tourneeDuLivreur);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listTournee;
    }
    public static DemandeDeLivraison parseurDemandeDeLivraison(String cheminFichier) {
        System.out.println("Lecture et parsing de : " + cheminFichier);

        Map<Long, NoeudDePassage> noeudsExistants = new HashMap<>();
        Map<Long, NoeudDePassage> pickups = new HashMap<>();
        Map<Long, NoeudDePassage> deliveries = new HashMap<>();
        NoeudDePassage entrepot = null;

        try {
            String contenu = Files.readString(Paths.get(cheminFichier));
            JSONObject root = new JSONObject(contenu);
            JSONArray tournees = root.getJSONArray("tournees");

            for (int k = 0; k < tournees.length(); k++) {
                JSONObject tourneeObject = tournees.getJSONObject(k);
                JSONArray cheminsArray = tourneeObject.getJSONArray("chemins");

                for (int i = 0; i < cheminsArray.length(); i++) {
                    JSONObject cheminObject = cheminsArray.getJSONObject(i);

                    JSONObject departJson = cheminObject.getJSONObject("NoeudDePassageDepart");
                    JSONObject arriveeJson = cheminObject.getJSONObject("NoeudDePassageArrivee");

                    NoeudDePassage depart = getOrCreateNoeud(departJson, noeudsExistants);
                    NoeudDePassage arrivee = getOrCreateNoeud(arriveeJson, noeudsExistants);

                    if (depart.getType() == NoeudDePassage.TypeNoeud.ENTREPOT && entrepot == null) {
                        entrepot = depart;
                    }

                    if (departJson.has("idDeliveryAssocie")) {
                        long idDelivery = departJson.getLong("idDeliveryAssocie");
                        pickups.put(idDelivery, depart);
                    }
                    if (arriveeJson.has("idPickupAssocie")) {
                        long idPickup = arriveeJson.getLong("idPickupAssocie");
                        deliveries.put(idPickup, arrivee);
                    }
                }
            }

            List<Livraison> livraisons = new ArrayList<>();
            for (Map.Entry<Long, NoeudDePassage> entry : pickups.entrySet()) {
                long idDelivery = entry.getKey();
                NoeudDePassage pickup = entry.getValue();
                NoeudDePassage delivery = deliveries.get(pickup.getId());
                if (delivery == null) {
                    delivery = deliveries.get(idDelivery);
                }
                if (delivery != null) {
                    livraisons.add(new Livraison(pickup, delivery));
                }
            }

            if (entrepot == null) {
                throw new IllegalStateException("Aucun entrepôt trouvé dans le fichier JSON.");
            }

            return new DemandeDeLivraison(entrepot, livraisons);

        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de " + cheminFichier + " : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private static NoeudDePassage getOrCreateNoeud(JSONObject noeudJson, Map<Long, NoeudDePassage> cache) {
        long id = noeudJson.getLong("id");
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        double latitude = noeudJson.getDouble("latitude");
        double longitude = noeudJson.getDouble("longitude");
        NoeudDePassage.TypeNoeud type = NoeudDePassage.TypeNoeud.valueOf(noeudJson.getString("typeNoeud"));

        NoeudDePassage noeud = new NoeudDePassage(id, latitude, longitude, type);
        cache.put(id, noeud);
        return noeud;
    }

}
