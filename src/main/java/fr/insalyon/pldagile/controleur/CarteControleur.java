package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
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
public class CarteControleur {
    @PostMapping({"/upload"})
    public ResponseEntity<?> uploadXML(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun fichier fourni");
            } else {
                File tempFile = File.createTempFile("carte-", ".xml");
                file.transferTo(tempFile);
                Carte carte = CarteParseurXML.loadFromFile(tempFile);
                tempFile.delete();
                return ResponseEntity.ok(carte);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}