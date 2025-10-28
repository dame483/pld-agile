package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

public interface Etat {

    Carte loadCarte(Controlleur c, MultipartFile file);
    Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte);
    Object uploadXML(String type, MultipartFile file, Carte carte);
    String getName();
    Object runCalculTournee(Controlleur c, int nombreLivreurs);
    Object creerFeuillesDeRoute(Controlleur c);
    Object saveTournee(Controlleur c);
    Object loadTournee(Controlleur c, MultipartFile file, Carte carte);

// void addLivraison(Controlleur c, MultipartFile file, Carte carte);
// void deleteLivraison(Controlleur c);
}