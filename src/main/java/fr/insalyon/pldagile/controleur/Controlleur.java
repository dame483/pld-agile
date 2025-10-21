package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping({"/api"})
@CrossOrigin(
        origins = {"*"}
)
public class Controlleur {
    protected Etat etatActuelle;


    public Controlleur() {
         etatActuelle= new EtatInitial();
    }


    @PostMapping({"/upload-carte"})
    public ResponseEntity<?> loadCarte(@RequestParam("file") MultipartFile file) {

        Carte carte = etatActuelle.loadCarte(this,file);

        if (carte != null) {
            Map<String, Object> response = Map.of(
                    "message", "La carte est bien chargé ",
                    "etatCourant", this.getCurrentState(),
                    "carte", carte
            );
            return ResponseEntity.ok(response);
        }
        else{
            return ResponseEntity.badRequest().body("Erreur ");
        }

    }

    @PostMapping({"/upload-demande"})
    public ResponseEntity<?> loadDemandeLivraison(@RequestParam("file") MultipartFile file, Carte carte){
            DemandeDeLivraison demande=etatActuelle.loadDemandeLivraison(this,file,carte);
            if(demande != null){
                Map<String, Object> response = Map.of(
                        "message", "La demande est bien chargé ",
                        "etatCourant", this.getCurrentState(),
                        "fichier", demande
                );
                return ResponseEntity.ok(response);
            }
            else{
                return ResponseEntity.badRequest().body("Erreur ");
            }

    }

    /*@PostMapping({"/upload-demande"})
    public void addLivraison(@RequestParam("file") MultipartFile file, Carte carte){
        etatActuelle.addLivraison(this, file,carte);
    }

    public void deleteLivraison(Carte carte){
        etatActuelle.deleteLivraison(this);
    }

    public void runCalculTournee(){
        etatActuelle.runCalculTournee(this);
    }
    public void saveTournee(){
        etatActuelle.saveTournee(this);
    }*/
    /*public void leftClick(){

    }
    public void rightClick(){

    }*/



    public void setCurrentState(Etat etat) {
        this.etatActuelle = etat;
    }
    public String getCurrentState() {
        return etatActuelle.getName();
    }
}


