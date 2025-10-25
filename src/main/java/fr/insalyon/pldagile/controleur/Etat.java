package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import org.springframework.web.multipart.MultipartFile;

public interface Etat {

    // Charger la carte
    Carte loadCarte(Controlleur c, MultipartFile file);

    // Charger la demande de livraison
    Object loadDemandeLivraison(Controlleur c, MultipartFile file, Carte carte);

    // Charger un fichier XML générique (peut être utilisé pour carte ou demande)
    Object uploadXML(String type, MultipartFile file, Carte carte);

    // Retourne le nom de l'état courant (utile pour le debug ou le front)
    String getName();

// (optionnel) : autres méthodes futures si tu veux ajouter des actions
// void addLivraison(Controlleur c, MultipartFile file, Carte carte);
// void deleteLivraison(Controlleur c);
   Object runCalculTournee(Controlleur c);
// void saveTournee(Controlleur c);

}
