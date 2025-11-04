package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.GestionnaireException;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.sortie.reponse.ApiReponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import fr.insalyon.pldagile.sortie.TourneeUpload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controlleur {

    private Etat etatActuelle;
    private Carte carte;
    private DemandeDeLivraison demande;
    private ListeDeCommandes historique;
    private double vitesse = 4.1;


    public Controlleur() {
        this.etatActuelle = new EtatInitial();
        this.historique = new ListeDeCommandes();
    }

    @PostMapping("/upload-carte")
    public ResponseEntity<ApiReponse> loadCarte(@RequestParam("file") MultipartFile file) {
        try {
            Carte newCarte = etatActuelle.loadCarte(this, file);

            if (newCarte != null) {
                this.carte = newCarte;
                this.demande = null;

                return ResponseEntity.ok(ApiReponse.succes(( "Carte chargée avec succès"), Map.of(
                        "etatCourant", getCurrentState().getName(),
                        "carte", newCarte
                )));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : carte non chargée"));
            }

        }
        catch (XMLFormatException e) {
            GestionnaireException gestionnaireException = new GestionnaireException();
            return gestionnaireException.handleException(e);
        } catch (Exception e) {
            GestionnaireException gestionnaireException = new GestionnaireException();
            return gestionnaireException.handleException(e);
        }
    }


    @PostMapping("/upload-demande")
    public ResponseEntity<ApiReponse> loadDemandeLivraison(@RequestParam("file") MultipartFile file) {
        try {
            if (this.carte == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Veuillez d'abord charger une carte avant la demande !"));
            }

            Object result = etatActuelle.loadDemandeLivraison(this, file, this.carte);

            if (result instanceof DemandeDeLivraison demande) {
                this.demande = demande; //
                return ResponseEntity.ok(ApiReponse.succes(("Demande chargée avec succès"), Map.of(
                        "etatCourant", getCurrentState().getName(),
                        "demande", demande
                )));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors du chargement de la demande"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }



    @PostMapping("/tournee/calculer")
    public ResponseEntity<ApiReponse> calculerTournee(@RequestParam(defaultValue = "1") int nombreLivreurs) {
        try {
            Object result = etatActuelle.runCalculTournee(this, nombreLivreurs, vitesse);

            if (result instanceof List<?> listeTournees) {
                // Crée un Map explicite pour le JSON
                Map<String, Object> response = new HashMap<>();
                response.put("tournees", listeTournees);
                return ResponseEntity.ok(ApiReponse.succes("Tournées calculées avec succès",response));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur( e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur( "Erreur inconnue"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    @PostMapping("/tournee/feuille-de-route")
    public ResponseEntity<ApiReponse> creerFeuilleDeRoute() {
        try {
            if (!(etatActuelle instanceof EtatTourneeCalcule)) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("La tournée n'est pas encore calculée."));
            }

            Object result = etatActuelle.creerFeuillesDeRoute(this);

            if (result instanceof String message) {
                return ResponseEntity.ok(ApiReponse.succes( "les feuilles de route ont été générées avec succès", Map.of(
                        "message", message
                )));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors de la génération de la feuille de route."));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }

    @PostMapping("/tournee/sauvegarde")
    public ResponseEntity<ApiReponse> saveTournee() {
        try {
            if (!(etatActuelle instanceof EtatTourneeCalcule)) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("La tournée n'est pas encore calculée."));
            }

            Object result = etatActuelle.saveTournee(this);

            if (result instanceof String message) {
                return ResponseEntity.ok(ApiReponse.succes("La tournée a été sauvegardée",Map.of(
                        "message", message
                )));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors de la sauvegarde de la tournée."));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }

    @PostMapping("/upload-tournee")
    public ResponseEntity<ApiReponse> uploadTournee(@RequestParam("file") MultipartFile file) {
        try {
            Object result = etatActuelle.loadTournee(this, file, this.carte);

            if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception" + e.getMessage()));
            }

            if (result instanceof TourneeUpload upload) {
                return ResponseEntity.ok(ApiReponse.succes("Tournées chargées avec succès", Map.of(
                        "tournees", upload.getTournee(),
                        "demande", upload.getDemande()
                )));
            }

            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors du chargement des tournées"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiReponse.erreur("Erreur serveur : " + e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiReponse> resetApplication() {
        try {
            this.carte = null;
            this.demande = null;
            this.etatActuelle = new EtatInitial();

            return ResponseEntity.ok(ApiReponse.succes("Application réinitialisée avec succès.", Map.of(
                    "etatCourant", getCurrentState()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur lors de la réinitialisation : " + e.getMessage()));
        }
    }

    @GetMapping("/etat")
    public ResponseEntity<ApiReponse> getEtatActuel() {
        return ResponseEntity.ok(ApiReponse.succes( "Nous sommes dans l'état" + etatActuelle.getName(), Map.of(
                "etat", etatActuelle.getName(),
                "carteChargee", carte != null,
                "demandeChargee", demande != null,
                "tourneeChargee", etatActuelle instanceof EtatTourneeCalcule
        )));
    }

    @PostMapping("/tournee/mode-modification")
    public ResponseEntity<ApiReponse> passerEnModeModification(@RequestBody Tournee tourneeCible) {
        try {
            if (tourneeCible == null)
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Aucune tournée fournie."));

            if (etatActuelle instanceof EtatTourneeCalcule etatTourneeCalcule) {
                etatTourneeCalcule.passerEnModeModification(this, tourneeCible);
                return ResponseEntity.ok(ApiReponse.succes("Passage en mode modification effectué.", Map.of(
                        "etatCourant", getCurrentState().getName()
                )));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Impossible depuis l'état actuel : " + getCurrentState()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
        }
    }


    @PostMapping("/tournee/modifier")
    public ResponseEntity<ApiReponse> modifierTournee(@RequestBody Map<String, Object> body) {
        try {
            String mode = (String) body.get("mode"); // "ajouter" ou "supprimer"

            if (mode == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Le mode doit être précisé (ajouter/supprimer)."));
            }

            if (!(etatActuelle instanceof EtatModificationTournee etatModif)) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("L'application n'est pas en mode modification."));
            }

            Map<String, Object> params = new HashMap<>();
            params.put("idNoeudPickup", ((Number) body.get("idNoeudPickup")).longValue());
            params.put("idNoeudDelivery", ((Number) body.get("idNoeudDelivery")).longValue());

            if (mode.equalsIgnoreCase("ajouter")) {
                params.put("idPrecedentPickup", ((Number) body.get("idPrecedentPickup")).longValue());
                params.put("idPrecedentDelivery", ((Number) body.get("idPrecedentDelivery")).longValue());
                params.put("dureeEnlevement", ((Number) body.get("dureeEnlevement")).doubleValue());
                params.put("dureeLivraison", ((Number) body.get("dureeLivraison")).doubleValue());
            } else if (!mode.equalsIgnoreCase("supprimer")) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur( "Mode inconnu : " + mode));
            }

            etatModif.modifierTournee(this, mode, params, vitesse);

            return ResponseEntity.ok(ApiReponse.succes("Opération effectuée : " + mode,Map.of(
                    "tournee", etatModif.getTournee(),
                    "etatCourant", getCurrentState().getName()
            )));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }





    @PostMapping("/annuler")
    public ResponseEntity<ApiReponse> annuler() {
        try {
            this.annulerCommande();

            // Récupère la tournée actuelle après annulation
            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(ApiReponse.succes("Annulation effectuée", Map.of(
                    "tournee", tourneeActuelle
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    @PostMapping("/restaurer")
    public ResponseEntity<ApiReponse> restaurer() {
        try {
            this.restaurerCommande();

            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(ApiReponse.succes("Rétablissement effectué", Map.of(
                    "tournee", tourneeActuelle  // ← Retourne l'état après restauration
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }




    public void executerCommande(Commande commande) {
        historique.executerCommande(commande);
    }

    public void annulerCommande() {
        historique.annuler();
    }

    public void restaurerCommande() {
        historique.restaurer();
    }


    public void setCurrentState(Etat etat) {
        this.etatActuelle = etat;
    }

    public Etat getCurrentState() {
        return etatActuelle;
    }

    public Carte getCarte() {
        return carte;
    }

    public DemandeDeLivraison getDemande() {
        return demande;
    }


}
