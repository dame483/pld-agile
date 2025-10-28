package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import org.springframework.web.multipart.MultipartFile;

public interface Etat {

    Carte loadCarte(Controlleur c, MultipartFile file);
    Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte);
    Object uploadXML(String type, MultipartFile file, Carte carte);
    String getName();
    Object runCalculTournee(Controlleur c);
    //void addLivraison(Controlleur c, MultipartFile file, Carte carte);
    //void deleteLivraison(Controlleur c);
    Object saveTournee(Controlleur c);

}
