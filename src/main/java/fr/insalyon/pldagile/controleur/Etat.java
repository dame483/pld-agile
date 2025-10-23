package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


public interface Etat{


    public Carte loadCarte(Controlleur c,@RequestParam("file") MultipartFile file) ;
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte);
    /*public void addLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte);
    public void deleteLivraison(Controlleur c);*/
    //public Object runCalculTournee(Controlleur c);
    /*public void saveTournee(Controlleur c);*/
    public Object uploadXML(String type, @RequestParam("file") MultipartFile file, Carte carte);
    public String getName();
    /*public void leftClick();
    public void rightClick();*/


}