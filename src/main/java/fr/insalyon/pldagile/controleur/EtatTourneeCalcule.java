package fr.insalyon.pldagile.controleur;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Component
public class  EtatTourneeCalcule implements Etat
{
    public EtatTourneeCalcule()
    {

    }
    @Override
    public Carte loadCarte(Controlleur c,@RequestParam("file") MultipartFile file){
    return null;
    }

    @Override
    public DemandeDeLivraison loadDemandeLivraison(Controlleur c, @RequestParam("file")  MultipartFile file, Carte carte) {
        return null;
    }



    /*@Override
    public void addLivraison(Controlleur c,@RequestParam("file") MultipartFile file, Carte carte){

    }

    @Override
    public void deleteLivraison(Controlleur c) {

    }

    @Override
    public void runCalculTournee(Controlleur c) {

    }

    @Override
    public void saveTournee(Controlleur c) {

    }*/


    @Override
    public ResponseEntity<?> uploadXML(String type, @RequestParam("file") MultipartFile file,Carte carte ){

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun fichier fourni");
            } else {
                File tempFile = File.createTempFile(type+"-", ".xml");
                file.transferTo(tempFile);
                if(type=="carte") {
                    carte = CarteParseurXML.loadFromFile(tempFile);
                    tempFile.delete();
                    return ResponseEntity.ok(carte);
                }
                else{
                    DemandeDeLivraison demande;
                    try {
                        file.transferTo(tempFile);
                        demande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, carte);
                    } finally {
                        tempFile.delete();
                    }

                    return ResponseEntity.ok(demande);

                }

            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "Etat Tournee Calcule";
    }
}