package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.erreurs.exception.ContrainteDePrecedenceException;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.TourneeUpload;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Représente l'état de modification d'une tournée déjà calculée dans l'application.
 * Dans cet état, l'utilisateur peut modifier une tournée existante (ajouter ou supprimer des livraisons),
 * mais certaines opérations comme la création de feuilles de route ou la sauvegarde directe d'une
 * tournée ne sont pas autorisées et renverront des messages d'erreur.
 *
 * <p>Les méthodes de chargement permettent :
 * <ul>
 *     <li>De recharger la carte associée (loadCarte).</li>
 *     <li>De charger une demande de livraison supplémentaire (loadDemandeLivraison).</li>
 *     <li>De recharger une tournée depuis un fichier JSON/XML (loadTournee).</li>
 * </ul>
 *
 */
public class EtatModificationTournee implements Etat {

    /** La carte représentant le réseau routier utilisé pour les tournées. */
    private Carte carte;
    /** La tournée actuellement modifiée, contenant les livraisons et leur ordre. */
    private Tournee tournee;
    /**
     * Constructeur pour initialiser l'état avec une carte et une tournée existantes.
     *
     * @param carte Carte sur laquelle la tournée est basée
     * @param tournee Tournée à modifier
     */
        public EtatModificationTournee(Carte carte, Tournee tournee) {
            this.carte = carte;
            this.tournee = tournee;
        }

    /**
     * Recharge une carte depuis un fichier XML. Si le chargement échoue, l'état revient à {@link EtatInitial}.
     *
     * @param c Contrôleur principal
     * @param file Fichier XML de la carte
     * @return Carte chargée ou null si échec
     */
    @Override
    public Carte chargerCarte(Controleur c, @RequestParam("file") MultipartFile file) {
        throw new IllegalStateException("Erreur : impossible de charger une carte en mode modification.");
    }
    /**
     * Charge une demande de livraison supplémentaire à partir d'un fichier XML.
     * Si la demande est valide, l'état passe à {@link EtatDemandeLivraisonChargee}.
     *
     * @param c Contrôleur principal
     * @param file Fichier XML de la demande
     * @param carte Carte associée
     * @return Objet de la demande chargée ou null/Exception si échec
     */
    @Override
    public Object chargerDemandeLivraison(Controleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une demande de livraison en mode modification.");
    }

    /**
     * Le calcul de tournée n'est pas exécuté en mode modification.
     */
    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : impossible de recalculer les tournées en mode modification.");
    }

    /**
     * Upload et parse un fichier XML ou JSON pour carte, demande ou tournée.
     *
     * @param type Type de fichier ("carte", "demande" ou "tournee")
     * @param file Fichier XML/JSON à traiter
     * @param carte Carte existante si nécessaire
     * @return Objet correspondant au type de fichier chargé
     * @throws XMLFormatException Si le fichier est invalide ou le type non reconnu
     */
    @Override
    public Object chargerXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        throw new IllegalStateException("Erreur : impossible de charger un fichier en mode modification de tournée.");
    }
    /**
     * Impossible de créer des feuilles de route en mode modification.
     */
    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route en mode modification de tournée.");
    }
    /**
     * Impossible de sauvegarder directement la tournée en mode modification.
     */
    @Override
    public Object sauvegarderTournee(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de sauvegarder une tournée complète en mode modification.");
    }
    /**
     * Charge une tournée depuis un fichier JSON ou XML. Met à jour l'état vers {@link EtatTourneeCalcule}.
     *
     * @param c Contrôleur principal
     * @param file Fichier contenant la tournée
     * @param carte Carte associée
     * @return Objet {@link TourneeUpload} ou Exception si le chargement échoue
     */
    @Override
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une tournée en mode modification.");
    }
    /**
     * Pas de passage en mode modification supplémentaire dans cet état.
     */
    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        throw new IllegalStateException("Erreur : Impossible de passer en mode modification à l’état initial.");
    }

    /**
     * Modifie la tournée selon le mode spécifié ("ajouter" ou "supprimer") en exécutant la commande correspondante.
     *
     * @param c Contrôleur principal
     * @param mode Mode de modification ("ajouter" ou "supprimer")
     * @param body Données nécessaires à la modification
     * @param vitesse Vitesse du livreur
     */
    @Override
    public Tournee modifierTournee(Controleur c, String mode, Map<String, Object> body, double vitesse) {
        Commande commande;

        switch (mode.toLowerCase()) {
            case "supprimer" -> {
                if (!body.containsKey("idNoeudPickup") || !body.containsKey("idNoeudDelivery")) {
                    throw new IllegalArgumentException("Paramètres manquants pour suppression.");
                }

                long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();

                commande = new CommandeSuppressionLivraison(tournee, carte, vitesse, idPickup, idDelivery);
            }

            case "ajouter" -> {
                String[] requiredKeys = {
                        "idNoeudPickup", "idNoeudDelivery",
                        "idPrecedentPickup", "idPrecedentDelivery",
                        "dureeEnlevement", "dureeLivraison"
                };
                for (String key : requiredKeys) {
                    if (!body.containsKey(key)) {
                        throw new IllegalArgumentException("Paramètre manquant pour ajout : " + key);
                    }
                }

                long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();
                long idPrecedentPickup = ((Number) body.get("idPrecedentPickup")).longValue();
                long idPrecedentDelivery = ((Number) body.get("idPrecedentDelivery")).longValue();
                double dureeEnlevement = ((Number) body.get("dureeEnlevement")).doubleValue();
                double dureeLivraison = ((Number) body.get("dureeLivraison")).doubleValue();

                ModificationTournee modificationTournee = new ModificationTournee(new CalculChemins(carte), vitesse);
                boolean respectContrainte = modificationTournee.contrainteDePrecedence(tournee, idDelivery, idPrecedentDelivery, idPrecedentPickup);
                if (!respectContrainte) {
                    throw new ContrainteDePrecedenceException("La contrainte de précédence n'est pas respectée pour le delivery");
                }
                commande = new CommandeAjoutLivraison(
                        tournee, carte, vitesse,
                        idPickup, idDelivery,
                        idPrecedentPickup, idPrecedentDelivery,
                        dureeEnlevement, dureeLivraison
                );
            }

            default -> throw new IllegalArgumentException("Mode de modification inconnu : " + mode);
        }

        c.executerCommande(commande);
        return this.tournee;
    }


    /**
     * Sauvegarde les modifications de la tournée et met à jour l'état vers {@link EtatTourneeCalcule}.
     *
     * @param c Contrôleur principal
     * @param demande Demande de livraison associée
     * @param tournees Liste des tournées modifiées
     */
    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        if (tournees == null || tournees.isEmpty()) {
            throw new IllegalArgumentException("Aucune tournée à sauvegarder.");
        }
        c.setEtatActuelle(new EtatTourneeCalcule(carte, demande, tournees));
    }
    /**
     * Retourne le nom de l'état courant.
     */
    @Override
    public String getNom() {
            return "ModeModificationTournee";
    }
    /**
     * Retourne la tournée actuellement modifiée.
     */
    public Tournee getTournee() {
            return tournee;
    }
}

