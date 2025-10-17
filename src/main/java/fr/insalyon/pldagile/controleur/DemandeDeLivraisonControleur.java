package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;
import java.io.File;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api"})
@CrossOrigin(
    origins = {"*"}
)
public class DemandeDeLivraisonControleur {
    private Carte carte;

    @PostMapping({"/upload-carte"})
    public ResponseEntity<?> uploadCarte(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun fichier fourni");
            } else {
                File tempFile = File.createTempFile("carte-", ".xml");

                try {
                    file.transferTo(tempFile);
                    this.carte = CarteParseurXML.loadFromFile(tempFile);
                } finally {
                    tempFile.delete();
                }

                return ResponseEntity.ok(this.carte);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping({"/upload-demande"})
    public ResponseEntity<?> uploadDemande(@RequestParam("file") MultipartFile file) {
        if (this.carte == null) {
            return ResponseEntity.badRequest().body("La carte doit être chargée avant la demande de livraison.");
        } else {
            try {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body("Aucun fichier fourni");
                } else {
                    File tempFile = File.createTempFile("demande-", ".xml");

                    DemandeDeLivraison demande;
                    try {
                        file.transferTo(tempFile);
                        demande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                    } finally {
                        tempFile.delete();
                    }

                    return ResponseEntity.ok(demande);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
            }
        }
    }
}