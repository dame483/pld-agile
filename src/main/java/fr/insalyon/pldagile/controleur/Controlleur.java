package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
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
            Object demande=etatActuelle.loadDemandeLivraison(this,file,carte);


            if(demande instanceof  DemandeDeLivraison){

                Map<String, Object> response = Map.of(
                        "message", "La demande est bien chargé ",
                        "etatCourant", this.getCurrentState(),
                        "demande", (DemandeDeLivraison)demande
                );
                System.out.println("Bipboup je suis une Demande de Livraison");
                System.out.println((DemandeDeLivraison) demande);
                return ResponseEntity.ok(response);
            }
            else if (demande instanceof Exception) {
                String errorMes=((Exception) demande).getMessage();
                return ResponseEntity.badRequest().body("Erreur : "+errorMes);

        } else{
                return ResponseEntity.badRequest().body("Erreur  ");
            }

    }

    @PostMapping({"/tournee/calculer"})
    public ResponseEntity<?> runCalculTournee(){
        Object tournee= etatActuelle.runCalculTournee(this);

        if(tournee instanceof Tournee){

            Map<String, Object> response = Map.of(
                    "message", "",
                    "etatCourant", this.getCurrentState(),
                    "demande", (Tournee)tournee
            );

            return ResponseEntity.ok(response);
        }
        else if (tournee instanceof Exception) {
            String errorMes=((Exception) tournee).getMessage();
            return ResponseEntity.badRequest().body("Erreur : "+errorMes);

        } else{
            return ResponseEntity.badRequest().body("Erreur  ");
        }
    }
    /*public void saveTournee(){
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


