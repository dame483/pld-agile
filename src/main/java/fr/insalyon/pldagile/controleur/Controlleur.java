package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controlleur {


    // 🧩 Etat courant de l'application (machine à états)
    private Etat etatActuelle;

    // 🔹 Données principales conservées en mémoire
    private Carte carte;
    private DemandeDeLivraison demande;

    // 🏁 Constructeur : état initial au démarrage
    public Controlleur() {
        this.etatActuelle = new EtatInitial();
    }

    @PostMapping("/upload-carte")
    public ResponseEntity<?> loadCarte(@RequestParam("file") MultipartFile file) {
        // ⚠️ Réinitialisation complète
        this.carte = null;
        this.demande = null;

        // 🔹 Remettre l'état à initial
        this.setCurrentState(new EtatInitial());

        Carte newCarte = etatActuelle.loadCarte(this, file);
        if (newCarte != null) {
            this.carte = newCarte;
            return ResponseEntity.ok(Map.of(
                    "message", "Carte chargée",
                    "etatCourant", getCurrentState(),
                    "carte", newCarte
            ));
        } else {
            return ResponseEntity.badRequest().body("Erreur : carte non chargée");
        }
    }



    // 🚚 Étape 2 : Charger la demande de livraison
    @PostMapping("/upload-demande")
    public ResponseEntity<?> loadDemandeLivraison(@RequestParam("file") MultipartFile file) {
        try {
            // ⚠️ sécurité : il faut une carte avant de charger la demande
            if (this.carte == null) {
                return ResponseEntity.badRequest().body("❌ Veuillez d'abord charger une carte avant la demande !");
            }

            Object result = etatActuelle.loadDemandeLivraison(this, file, this.carte);

            if (result instanceof DemandeDeLivraison demande) {
                this.demande = demande; // ✅ garder la demande en mémoire
                return ResponseEntity.ok(Map.of(
                        "message", "✅ Demande chargée avec succès",
                        "etatCourant", getCurrentState(),
                        "demande", demande
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("❌ Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("❌ Erreur inconnue lors du chargement de la demande");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Exception : " + e.getMessage());
        }
    }

    // 🔄 Setter pour changer d'état (utilisé par les classes d'état)
    public void setCurrentState(Etat etat) {
        this.etatActuelle = etat;
    }

    // 🔍 Pour connaître le nom de l'état actuel (affiché dans les réponses)
    public String getCurrentState() {
        return etatActuelle.getName();
    }

    // 🧠 (Optionnel) : Getter si tu veux exposer la carte ou la demande ailleurs
    public Carte getCarte() {
        return carte;
    }

    public DemandeDeLivraison getDemande() {
        return demande;
    }

    @PostMapping("/tournee/calculer")
    public ResponseEntity<?> calculerTournee() {
        try {
            Object result = etatActuelle.runCalculTournee(this);

            if (result instanceof Tournee) {
                return ResponseEntity.ok(Map.of(
                        "message", "Tournée calculée",
                        "tournee", result
                ));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Erreur inconnue");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception : " + e.getMessage());
        }
    }


}
