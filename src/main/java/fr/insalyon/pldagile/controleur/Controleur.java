package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Noeud;
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
public class Controleur {

    private Etat etatActuelle;
    private Carte carte;
    private DemandeDeLivraison demande;
    private ListeDeCommandes historique;
    private double vitesse = 4.1;


    public Controleur() {
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
                        "etatCourant", getCurrentState(),
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
                        "etatCourant", getCurrentState(),
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
                        "etatCourant", getCurrentState(),
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

    @PostMapping("/tournee/mode-suppression")
    public ResponseEntity<?> passerEnModeSuppression(@RequestBody Tournee tourneeCible) {
        try {
            if (tourneeCible == null) {
                return ResponseEntity.badRequest().body("Aucune tournée fournie pour passer en mode suppression.");
            }

            // On délègue la logique à l’état courant (EtatTourneeCalcule)
            if (etatActuelle instanceof EtatTourneeCalcule etatTourneeCalcule) {
                etatTourneeCalcule.passerEnModeSuppression(this, tourneeCible);
                return ResponseEntity.ok(Map.of(
                        "message", "Passage en mode suppression effectué.",
                        "etatCourant", getCurrentState()
                ));
            } else {
                return ResponseEntity.badRequest().body(
                        "Impossible de passer en mode suppression depuis l'état actuel : " + getCurrentState()
                );
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @DeleteMapping("/livraison/supprimer")
    public ResponseEntity<?> supprimerLivraison(@RequestBody Map<String, Long> body) {
        try {
            Long idNoeudClique = body.get("idNoeudClique");
            Long idNoeudAssocie = body.get("idNoeudAssocie");

            if (!(etatActuelle instanceof EtatSuppressionLivraison etatSuppression)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Action impossible : l'application n'est pas en mode suppression."));
            }

            if (idNoeudClique == null || idNoeudAssocie == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Identifiants de nœuds manquants."));
            }

            if (idNoeudClique.equals(idNoeudAssocie)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Les nœuds de pickup et de livraison doivent être différents."));
            }


            // Appel du pattern Commande via l'état courant
            etatSuppression.supprimmerLivraison(this, idNoeudClique, idNoeudAssocie, vitesse);

            return ResponseEntity.ok(Map.of(
                    "message", "Livraison supprimée avec succès.",
                    "etatCourant", getCurrentState(),
                    "tournee", etatSuppression.getTournee()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur : " + e.getMessage()));
        }
    }


    // --- Annuler la dernière commande ---
    @PostMapping("/annuler")
    public ResponseEntity<?> annuler() {
        try {
            this.annulerCommande();
            return ResponseEntity.ok(Map.of(
                    "message", "Annulation effectuée"

            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de l'annulation : " + e.getMessage()));
        }
    }

    // --- Refaire la dernière commande annulée ---
    @PostMapping("/restaurer")
    public ResponseEntity<?> restaurer() {
        try {
            this.restaurerCommande();
            return ResponseEntity.ok(Map.of(
                    "message", "Rétablissement effectué"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors du rétablissement : " + e.getMessage()));
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

    public String getCurrentState() {
        return etatActuelle.getName();
    }

    public Carte getCarte() {
        return carte;
    }

    public DemandeDeLivraison getDemande() {
        return demande;
    }


}
