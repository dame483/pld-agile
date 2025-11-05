package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface Etat {

    Carte loadCarte(Controlleur c, MultipartFile file);
    Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte);
    Object uploadXML(String type, MultipartFile file, Carte carte);
    String getName();
    Object runCalculTournee(Controlleur c, int nombreLivreurs, double vitesse);
    List<Path> creerFeuillesDeRoute(Controlleur c);
    Object saveTournee(Controlleur c);
    Object loadTournee(Controlleur c, MultipartFile file, Carte carte);
    void sauvegarderModification(Controlleur c, DemandeDeLivraison demande, List<Tournee> tournees);
    void passerEnModeModification(Controlleur c, Tournee tournee);

}