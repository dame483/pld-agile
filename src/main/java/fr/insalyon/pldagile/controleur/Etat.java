package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Interface représentant l'état de l'application.
 * Définit les actions possibles selon l'état courant.
 */
public interface Etat {

    /**
     * Charge une carte depuis un fichier.
     *
     * @param c Contrôleur de l'application.
     * @param file Fichier contenant la carte.
     * @return La carte chargée.
     */
    Carte chargerCarte(Controleur c, MultipartFile file);
    /**
     * Charge une demande de livraison depuis un fichier.
     *
     * @param c Contrôleur de l'application.
     * @param file Fichier de la demande de livraison.
     * @param carte Carte associée.
     * @return Objet représentant la demande de livraison.
     */
    Object chargerDemandeLivraison(Controleur c, MultipartFile file, Carte carte);
    /**
     * Upload d'un fichier XML et traitement selon le type.
     *
     * @param type Type de fichier.
     * @param file Fichier XML.
     * @param carte Carte associée.
     * @return Résultat du traitement.
     */
    Object chargerXML(String type, MultipartFile file, Carte carte);
    /**
     * Retourne le nom de l'état courant.
     */
    String getNom();
    /**
     * Lance le calcul des tournées.
     *
     * @param c Contrôleur de l'application.
     * @param nombreLivreurs Nombre de livreurs disponibles.
     * @param vitesse Vitesse utilisée pour le calcul.
     * @return Résultat du calcul.
     */
    Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse);
    /**
     * Crée les feuilles de route pour les tournées.
     *
     * @param c Contrôleur de l'application.
     * @return Liste des chemins vers les fichiers créés.
     */
    List<Path> creerFeuillesDeRoute(Controleur c);
    /**
     * Sauvegarde une tournée.
     *
     * @param c Contrôleur de l'application.
     * @return Objet représentant le résultat de la sauvegarde.
     */
    Object sauvegarderTournee(Controleur c);
    /**
     * Charge une tournée depuis un fichier.
     *
     * @param c Contrôleur de l'application.
     * @param file Fichier de la tournée.
     * @param carte Carte associée.
     * @return Tournée chargée.
     */
    Object chargerTournee(Controleur c, MultipartFile file, Carte carte);
    /**
     * Sauvegarde les modifications effectuées sur une demande et les tournées.
     *
     * @param c Contrôleur de l'application.
     * @param demande Demande de livraison modifiée.
     * @param tournees Liste des tournées modifiées.
     */
    void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees);
    /**
     * Passe en mode modification pour une tournée donnée.
     *
     * @param c Contrôleur de l'application.
     * @param tournee Tournée à modifier.
     */
    void passerEnModeModification(Controleur c, Tournee tournee);
    Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse);

}