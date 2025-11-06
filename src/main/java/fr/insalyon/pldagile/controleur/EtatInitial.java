package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Représente l'état initial de l'application, avant que toute carte ou demande de livraison
 * ne soit chargée. Dans cet état, seules les opérations de chargement de carte sont possibles.
 * Les autres opérations (demande de livraison, calcul de tournée, création de feuilles de route,
 * sauvegarde ou chargement de tournée) sont interdites et génèrent des exceptions ou des messages d'erreur.
 */
@Component
public class EtatInitial implements Etat {

    /**
     * Constructeur par défaut de l'état initial.
     */
    public EtatInitial() {}

    /**
     * Charge une carte depuis un fichier XML et change l'état en {@link EtatCarteChargee} si réussite.
     *
     * @param c Contrôleur principal
     * @param file Fichier XML contenant la carte
     * @return Carte chargée
     * @throws XMLFormatException si le fichier est invalide ou si le chargement échoue
     */
    @Override
    public Carte chargerCarte(Controleur c, MultipartFile file) throws XMLFormatException {
        Object result = chargerXML("carte", file, null);

        if (result instanceof Carte carte) {
            c.setEtatActuelle(new EtatCarteChargee(carte));
            return carte;
        } else if (result instanceof Exception e) {
            if (e instanceof XMLFormatException xmlEx) {
                throw xmlEx;
            } else {
                throw new XMLFormatException("Erreur lors du chargement de la carte : " + e.getMessage());
            }
        } else {
            throw new XMLFormatException("Fichier XML invalide ou carte non chargée.");
        }
    }



    /**
     * Upload et parse un fichier XML pour la carte.
     *
     * @param type Type de fichier ("carte")
     * @param file Fichier XML à traiter
     * @param carte Carte existante (non utilisée ici)
     * @return Carte chargée
     * @throws XMLFormatException si le fichier est invalide ou si une erreur se produit
     */
    @Override
    public Object chargerXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        if (file == null || file.isEmpty()) {
            throw new XMLFormatException("Fichier vide ou non sélectionné !");
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(type + "-", ".xml");
            file.transferTo(tempFile);

            return CarteParseurXML.loadFromFile(tempFile);

        } catch (XMLFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLFormatException("Erreur inattendue lors du chargement XML : " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("Impossible de supprimer le fichier temporaire");
                }
            }
        }
    }

    /**
     * Impossible de charger une demande sans carte préalablement chargée.
     */
    @Override

    public Object chargerDemandeLivraison(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : Impossible de charger une demande de livraison avant de charger une carte.");
    }

    /**
     * Impossible de créer des feuilles de route avant le calcul des tournées.
     */
    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route avant le calcul de la tournée.");
    }

    /**
     * Impossible de sauvegarder une tournée avant son calcul.
     */
    @Override
    public Object sauvegarderTournee(Controleur c) {
        throw new IllegalStateException("Erreur : Impossible de sauvegarder une tournée qui n'a pas été calculée.");
    }


    /**
     * Impossible de charger une tournée sans carte.
     */
    @Override
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : Impossible de charger une tournée sans carte préalablement chargée.");
    }

    /**
     * Pas de sauvegarde de modification possible dans cet état.
     */
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : Impossible de charger une tournée sans carte préalablement chargée.");
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        throw new IllegalStateException("Erreur : Aucune modification à sauvegarder à l’état initial.");
    }

    /**
     * Le calcul de tournée n'est pas possible dans cet état.
     */
    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : Impossible de calculer une tournée sans carte et demande de livraison.");
    }


    /**
     * Pas de passage en mode modification possible dans cet état.
     */
    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        throw new IllegalStateException("Erreur : Impossible de passer en mode modification à l’état initial.");
    }

    /**
     * Retourne le nom de l'état courant.
     */
    @Override
    public String getNom() {
        return "Etat Initial";
    }
}