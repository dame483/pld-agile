package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controleur {

    private Carte carte;
    private DemandeDeLivraison demande;

    @PostMapping("/upload-carte")
    public ResponseEntity<?> uploadCarte(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("Aucun fichier fourni");
            File tempFile = File.createTempFile("carte-", ".xml");
            try {
                file.transferTo(tempFile);
                this.carte = CarteParseurXML.loadFromFile(tempFile);
            } finally {
                tempFile.delete();
            }
            return ResponseEntity.ok(this.carte);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur carte : " + e.getMessage());
        }
    }

    @PostMapping("/upload-demande")
    public ResponseEntity<?> uploadDemande(@RequestParam("file") MultipartFile file) {
        if (this.carte == null) return ResponseEntity.badRequest().body("La carte doit être chargée avant la demande de livraison.");
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("Aucun fichier fourni");
            File tempFile = File.createTempFile("demande-", ".xml");
            try {
                file.transferTo(tempFile);
                this.demande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
            } finally {
                tempFile.delete();
            }
            return ResponseEntity.ok(this.demande);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur demande : " + e.getMessage());
        }
    }

    @PostMapping("/tournee/calculer")
    public ResponseEntity<?> calculerTournee() {
        try {
            if (this.carte == null || this.demande == null)
                return ResponseEntity.badRequest().body("Carte ou demande non chargée !");

            long startTime = System.currentTimeMillis();
            CalculTournee calc = new CalculTournee(this.carte, this.demande, 4.16, LocalTime.of(8, 0));
            Tournee tournee = calc.calculerTournee();
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 10000) return ResponseEntity.status(503).body("Calcul trop long, veuillez réessayer.");

            Map<String, Object> response = new HashMap<>();
            response.put("chemins", tournee.getChemins());
            response.put("distanceTotale", calc.getLongueurTotale());
            response.put("dureeTotale", calc.getDureeTotale());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du calcul de la tournée : " + e.getMessage());
        }
    }
}