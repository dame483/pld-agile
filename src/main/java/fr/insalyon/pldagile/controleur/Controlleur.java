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

    public Controlleur() {
        this.etatActuelle = new EtatInitial();
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
            Object result = etatActuelle.runCalculTournee(this, nombreLivreurs);

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

            if (result instanceof Tournee tournee) {
                this.etatActuelle = new EtatTourneeCalcule(this.carte, this.demande, tournee);
                return ResponseEntity.ok(Map.of(
                        "message", "Tournée chargée avec succès",
                        "etatCourant", getCurrentState(),
                        "tournee", tournee
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors du chargement de la tournée");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
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

            Object result = etatActuelle.uploadXML("tournee", file, this.carte);

            if (result instanceof Tournee tournee) {
                this.etatActuelle = new EtatTourneeCalcule(this.carte, this.demande, tournee);
                return ResponseEntity.ok(Map.of(
                        "message", "Tournée chargée avec succès",
                        "etatCourant", getCurrentState(),
                        "tournee", tournee
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue lors du chargement de la tournée");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
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
