package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

public interface Etat {

    Carte loadCarte(Controleur c, MultipartFile file);
    Object loadDemandeLivraison(Controleur c, MultipartFile file, Carte carte);
    Object uploadXML(String type, MultipartFile file, Carte carte);
    String getName();
    Object runCalculTournee(Controleur c, int nombreLivreurs, double vitesse);
    Object creerFeuillesDeRoute(Controleur c);
    Object saveTournee(Controleur c);
    Object loadTournee(Controleur c, MultipartFile file, Carte carte);
    public void passerEnModeSuppression(Controleur c, Tournee tournee);

// void addLivraison(Controleur c, MultipartFile file, Carte carte);
// void deleteLivraison(Controleur c);
}