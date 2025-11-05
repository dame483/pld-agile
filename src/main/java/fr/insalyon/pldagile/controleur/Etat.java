package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Etat {

    Carte loadCarte(Controleur c, MultipartFile file);
    Object loadDemandeLivraison(Controleur c, MultipartFile file, Carte carte);
    Object uploadXML(String type, MultipartFile file, Carte carte);
    String getName();
    Object runCalculTournee(Controleur c, int nombreLivreurs, double vitesse);
    List<Path> creerFeuillesDeRoute(Controleur c);
    Object saveTournee(Controleur c);
    Object loadTournee(Controleur c, MultipartFile file, Carte carte);
    void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees);
    void passerEnModeModification(Controleur c, Tournee tournee);
    Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse);

}