package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class SauvegarderTournee {
    private List<Tournee> listTournee;
    private DemandeDeLivraison demandeDeLivraison;

    public SauvegarderTournee(List<Tournee> listTournee, DemandeDeLivraison demandeDeLivraison) {
        this.listTournee = listTournee;
        this.demandeDeLivraison = demandeDeLivraison;
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
                    if (chemin.getNoeudDePassageDepart().getType().equals(NoeudDePassage.TypeNoeud.PICKUP)) {
                        long idDeliveryAssocie = getDeliveryAssocieNoeudDePassageDepart(chemin);
                        noeuddePassageDepart.put("idDeliveryAssocie", idDeliveryAssocie);
                    } else if (chemin.getNoeudDePassageDepart().getType().equals(NoeudDePassage.TypeNoeud.DELIVERY)) {
                        long idPickupAssocie = getPickupAssocieNoeudDePassageDepart(chemin);
                        noeuddePassageDepart.put("idPickupAssocie", idPickupAssocie);
                    }
                    noeuddePassageDepart.put("horaireArrivee", chemin.getNoeudDePassageDepart().getHoraireArrivee().toString());
                    noeuddePassageDepart.put("horaireDepart", chemin.getNoeudDePassageDepart().getHoraireDepart().toString());
                    noeuddePassageDepart.put("duree", chemin.getNoeudDePassageDepart().getDuree());
                    cheminObject.put("NoeudDePassageDepart", noeuddePassageDepart);

                    JSONObject noeudDePassageArrivee = new JSONObject();
                    noeudDePassageArrivee.put("id", chemin.getNoeudDePassageArrivee().getId());
                    noeudDePassageArrivee.put("latitude", chemin.getNoeudDePassageArrivee().getLatitude());
                    noeudDePassageArrivee.put("longitude", chemin.getNoeudDePassageArrivee().getLongitude());
                    noeudDePassageArrivee.put("typeNoeud", chemin.getNoeudDePassageArrivee().getType());
                    if (chemin.getNoeudDePassageArrivee().getType().equals(NoeudDePassage.TypeNoeud.PICKUP)) {
                        long idDeliveryAssocie = getDeliveryAssocieNoeudDePassageArrivee(chemin);
                        noeudDePassageArrivee.put("idDeliveryAssocie", idDeliveryAssocie);
                    } else if (chemin.getNoeudDePassageArrivee().getType().equals(NoeudDePassage.TypeNoeud.DELIVERY)) {
                        long idPickupAssocie = getPickupAssocieNoeudDePassageArrivee(chemin);
                        noeudDePassageArrivee.put("idPickupAssocie", idPickupAssocie);
                    }
                    noeudDePassageArrivee.put("horaireArrivee", chemin.getNoeudDePassageArrivee().getHoraireArrivee().toString());
                    noeudDePassageArrivee.put("horaireDepart", chemin.getNoeudDePassageArrivee().getHoraireDepart().toString());
                    noeudDePassageArrivee.put("duree", chemin.getNoeudDePassageArrivee().getDuree());
                    cheminObject.put("NoeudDePassageArrivee", noeudDePassageArrivee);

                    cheminsArray.put(cheminObject);
                }
                tourneeObject.put("dureeTotale", tournee.getDureeTotale());
                tourneeObject.put("chemins", cheminsArray);
                tourneesArray.put(tourneeObject);
            }

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                JSONObject root = new JSONObject();
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
    private long getPickupAssocieNoeudDePassageDepart(Chemin chemin) {
        Livraison livraisonAssocie = new Livraison();
        long idPickupAssocie = 0;
        for (Livraison livraison : demandeDeLivraison.getLivraisons()) {
            if (livraison.getAdresseLivraison().getId() == chemin.getNoeudDePassageDepart().getId() || livraison.getAdresseEnlevement().getId() == chemin.getNoeudDePassageDepart().getId()) {
                livraisonAssocie = livraison;
                break;
            }
        }
        if (chemin.getNoeudDePassageDepart().getType().equals(NoeudDePassage.TypeNoeud.DELIVERY)) {
             idPickupAssocie = livraisonAssocie.getAdresseEnlevement().getId();
        }

        return idPickupAssocie;
    }

    private long getPickupAssocieNoeudDePassageArrivee(Chemin chemin) {
        Livraison livraisonAssocie = new Livraison();
        long idPickupAssocie = 0;
        for (Livraison livraison : demandeDeLivraison.getLivraisons()) {
            if (livraison.getAdresseLivraison().getId() == chemin.getNoeudDePassageArrivee().getId() || livraison.getAdresseEnlevement().getId() == chemin.getNoeudDePassageArrivee().getId()) {
                livraisonAssocie = livraison;
                break;
            }
        }
        if (chemin.getNoeudDePassageArrivee().getType().equals(NoeudDePassage.TypeNoeud.DELIVERY)) {
            idPickupAssocie = livraisonAssocie.getAdresseEnlevement().getId();
        }

        return idPickupAssocie;
    }

    private long getDeliveryAssocieNoeudDePassageDepart(Chemin chemin) {
        Livraison livraisonAssocie = new Livraison();
        long idDeliveryAssocie = 0;
        for (Livraison livraison : demandeDeLivraison.getLivraisons()) {
            if (livraison.getAdresseLivraison().getId() == chemin.getNoeudDePassageDepart().getId() || livraison.getAdresseEnlevement().getId() == chemin.getNoeudDePassageDepart().getId()) {
                livraisonAssocie = livraison;
                if (chemin.getNoeudDePassageDepart().getType().equals(NoeudDePassage.TypeNoeud.PICKUP)) {
                    idDeliveryAssocie = livraisonAssocie.getAdresseLivraison().getId();
                    break;
                }
            }
        }


        return idDeliveryAssocie;
    }

    private long getDeliveryAssocieNoeudDePassageArrivee(Chemin chemin) {
        Livraison livraisonAssocie = new Livraison();
        long idDeliveryAssocie = 0;
        for (Livraison livraison : demandeDeLivraison.getLivraisons()) {
            if (livraison.getAdresseLivraison().getId() == chemin.getNoeudDePassageArrivee().getId() || livraison.getAdresseEnlevement().getId() == chemin.getNoeudDePassageArrivee().getId()) {
                livraisonAssocie = livraison;
                if (chemin.getNoeudDePassageArrivee().getType().equals(NoeudDePassage.TypeNoeud.PICKUP)) {
                    idDeliveryAssocie = livraisonAssocie.getAdresseLivraison().getId();
                    break;
                }
            }
        }


        return idDeliveryAssocie;
    }

}