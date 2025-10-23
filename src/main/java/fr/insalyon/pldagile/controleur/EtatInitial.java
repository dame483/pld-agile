package fr.insalyon.pldagile.controleur;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Component
public class  EtatInitial implements Etat {
    public EtatInitial() {}

    @Override
    public Carte loadCarte(Controlleur c, @RequestParam("file") MultipartFile file) {
        Carte carte = (Carte) uploadXML("carte", file, null);

        if (carte != null) {
            c.setCurrentState(new EtatCarteChargee(carte));
            return carte;
        }
        else return null;
    }

    @Override
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file")  MultipartFile file, Carte carte) {
        return null;
    }



    /*@Override
    public void addLivraison(Controlleur c,@RequestParam("file")  MultipartFile file, Carte carte) {

    }

    @Override
    public void deleteLivraison(Controlleur c) {

    }*/

    @Override
    public Object runCalculTournee(Controlleur c) {
        return null;
    }
/*
    @Override
    public void saveTournee(Controlleur c) {

    }*/

    @Override
    public Object uploadXML(String type,@RequestParam("file") MultipartFile file, Carte carte)
        {

        try {
            if (file.isEmpty()) {
                return null;
            } else {
                File tempFile = File.createTempFile(type + "-", ".xml");
                file.transferTo(tempFile);
                carte = CarteParseurXML.loadFromFile(tempFile);
                tempFile.delete();
                return carte;
            }
        } catch (Exception e) {
            return e;
        }
    }

    @Override
    public String getName() {
        return "Etat Initial";
    }
}