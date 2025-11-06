package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Etat {

    Carte chargerCarte(Controleur c, MultipartFile file);
    Object chargerDemandeLivraison(Controleur c, MultipartFile file, Carte carte);
    Object chargerXML(String type, MultipartFile file, Carte carte);
    String getNom();
    Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse);
    List<Path> creerFeuillesDeRoute(Controleur c);
    Object sauvegarderTournee(Controleur c);
    Object chargerTournee(Controleur c, MultipartFile file, Carte carte);
    void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees);
    void passerEnModeModification(Controleur c, Tournee tournee);
    Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse);

}