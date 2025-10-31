package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> loadCarte(@RequestParam("file") MultipartFile file) {
        try {
            Carte newCarte = etatActuelle.loadCarte(this, file);

            if (newCarte != null) {
                this.carte = newCarte;
                this.demande = null;

                return ResponseEntity.ok(Map.of(
                        "message", " Carte chargée avec succès",
                        "etatCourant", getCurrentState().getName(),
                        "carte", newCarte
                ));
            } else {
                return ResponseEntity.badRequest().body("Erreur : carte non chargée");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }

    }


    @PostMapping("/upload-demande")
    public ResponseEntity<?> loadDemandeLivraison(@RequestParam("file") MultipartFile file) {
        try {
            if (this.carte == null) {
                return ResponseEntity.badRequest().body("Veuillez d'abord charger une carte avant la demande !");
            }

            Object result = etatActuelle.loadDemandeLivraison(this, file, this.carte);

            if (result instanceof DemandeDeLivraison demande) {
                this.demande = demande; //
                return ResponseEntity.ok(Map.of(
                        "message", "Demande chargée avec succès",
                        "etatCourant", getCurrentState().getName(),
                        "demande", demande
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors du chargement de la demande");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
    }



    @PostMapping("/tournee/calculer")
    public ResponseEntity<?> calculerTournee(@RequestParam(defaultValue = "1") int nombreLivreurs) {
        try {
            Object result = etatActuelle.runCalculTournee(this, nombreLivreurs, vitesse);

            if (result instanceof List<?> listeTournees) {
                // Crée un Map explicite pour le JSON
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Tournées calculées");
                response.put("tournees", listeTournees);
                return ResponseEntity.ok(response);
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Erreur inconnue"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tournee/feuille-de-route")
    public ResponseEntity<?> creerFeuilleDeRoute() {
        try {
            if (!(etatActuelle instanceof EtatTourneeCalcule)) {
                return ResponseEntity.badRequest().body("La tournée n'est pas encore calculée.");
            }

            Object result = etatActuelle.creerFeuillesDeRoute(this);

            if (result instanceof String message) {
                return ResponseEntity.ok(Map.of(
                        "message", message
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors de la génération de la feuille de route.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
    }

    @PostMapping("/tournee/sauvegarde")
    public ResponseEntity<?> saveTournee() {
        try {
            if (!(etatActuelle instanceof EtatTourneeCalcule)) {
                return ResponseEntity.badRequest().body("La tournée n'est pas encore calculée.");
            }

            Object result = etatActuelle.saveTournee(this);

            if (result instanceof String message) {
                return ResponseEntity.ok(Map.of(
                        "message", message
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors de la sauvegarde de la tournée.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
    }

    @PostMapping("/upload-tournee")
    public ResponseEntity<?> loadTournee(@RequestParam("file") MultipartFile file) {
        try {
            if (this.carte == null) {
                return ResponseEntity.badRequest().body("Veuillez d'abord charger une carte avant la tournée !");
            }

            Object result = etatActuelle.loadTournee(this, file, this.carte);

            if (result instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
                List<Tournee> toutesLesTournees = (List<Tournee>) liste;

                this.etatActuelle = new EtatTourneeCalcule(this.carte, this.demande, toutesLesTournees);

                return ResponseEntity.ok(Map.of(
                        "message", "Tournées chargées avec succès",
                        "etatCourant", getCurrentState().getName(),
                        "tournees", toutesLesTournees
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors du chargement des tournées");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
    }

    @PostMapping("/tournee/mode-modification")
    public ResponseEntity<?> passerEnModeModification(@RequestBody Tournee tourneeCible) {
        try {
            if (tourneeCible == null)
                return ResponseEntity.badRequest().body("Aucune tournée fournie.");

            if (etatActuelle instanceof EtatTourneeCalcule etatTourneeCalcule) {
                etatTourneeCalcule.passerEnModeModification(this, tourneeCible);
                return ResponseEntity.ok(Map.of(
                        "message", "Passage en mode modification effectué.",
                        "etatCourant", getCurrentState().getName()
                ));
            } else {
                return ResponseEntity.badRequest().body("Impossible depuis l'état actuel : " + getCurrentState());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @PostMapping("/tournee/modifier")
    public ResponseEntity<?> modifierTournee(@RequestBody Map<String, Object> body) {
        try {
            String mode = (String) body.get("mode"); // "ajouter" ou "supprimer"

            if (mode == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Le mode doit être précisé (ajouter/supprimer)."));

            if (!(etatActuelle instanceof EtatModificationTournee etatModif))
                return ResponseEntity.badRequest().body(Map.of("message", "L'application n'est pas en mode modification."));


            if (mode.equalsIgnoreCase("supprimer")) {

                Long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                Long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();

                etatModif.modifierTournee(this, mode, Map.of(
                        "idNoeudPickup", idPickup,
                        "idNoeudDelivery", idDelivery
                ), vitesse);

            } else if (mode.equalsIgnoreCase("ajouter")) {

                Long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                Long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();
                Long idPrecedentPickup = ((Number) body.get("idPrecedentPickup")).longValue();
                Long idPrecedentDelivery = ((Number) body.get("idPrecedentDelivery")).longValue();

                etatModif.modifierTournee(this, mode, Map.of(
                        "idNoeudPickup", idPickup,
                        "idNoeudDelivery", idDelivery,
                        "idPrecedentPickup", idPrecedentPickup,
                        "idPrecedentDelivery", idPrecedentDelivery
                ), vitesse);

            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Mode inconnu : " + mode));
            }


            return ResponseEntity.ok(Map.of(
                    "message", "Opération effectuée : " + mode,
                    "tournee", etatModif.getTournee(),
                    "etatCourant", getCurrentState().getName()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }



    @PostMapping("/annuler")
    public ResponseEntity<?> annuler() {
        try {
            this.annulerCommande();

            // Récupère la tournée actuelle après annulation
            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Annulation effectuée",
                    "tournee", tourneeActuelle
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/restaurer")
    public ResponseEntity<?> restaurer() {
        try {
            this.restaurerCommande();

            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Rétablissement effectué",
                    "tournee", tourneeActuelle  // ← Retourne l'état après restauration
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
