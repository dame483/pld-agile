package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.ContrainteDePrecedenceException;
import fr.insalyon.pldagile.erreurs.exception.GestionnaireException;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.Tournee;
import fr.insalyon.pldagile.sortie.ModificationsDTO;
import fr.insalyon.pldagile.sortie.reponse.ApiReponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import fr.insalyon.pldagile.sortie.TourneeUpload;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Contrôleur principal de l'application PLD Agile.
 * <p>
 * Gère les requêtes REST pour le chargement de la carte, des demandes de livraison,
 * le calcul et la modification des tournées, ainsi que les fonctionnalités de sauvegarde et de réinitialisation.
 * <br>
 * Implémente le patron de conception <b>State</b> pour gérer les différents états
 * de l'application (initial, carte chargée, tournée calculée, modification, etc.).
 * </p>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controleur {

    /** État courant de l'application (pattern State). */
    private Etat etatActuelle;

    /** Carte routière actuellement chargée. */
    private Carte carte;

    /** Demande de livraison en cours. */
    private DemandeDeLivraison demande;

    /** Historique des commandes exécutées (pattern Command). */
    private ListeDeCommandes historique;

    /** Vitesse moyenne du livreur en m/s (par défaut 4.1). */
    private double vitesse = 4.1;

    /**
     * Constructeur par défaut.
     * Initialise l'état à {@link EtatInitial} et crée un historique de commandes vide.
     */
    public Controleur() {
        this.etatActuelle = new EtatInitial();
        this.historique = new ListeDeCommandes();
    }


    /**
     * Charge une carte XML et met à jour l'état de l'application.
     *
     * @param file fichier XML de la carte à charger
     * @return une {@link ResponseEntity} contenant un {@link ApiReponse} de succès ou d'erreur
     */
    @PostMapping("/upload-carte")
    public ResponseEntity<ApiReponse> chargerCarte(@RequestParam("file") MultipartFile file) {
        try {
            Carte newCarte = etatActuelle.chargerCarte(this, file);

            if (newCarte != null) {
                this.carte = newCarte;
                this.demande = null;

                return ResponseEntity.ok(ApiReponse.succes(( "Carte chargée avec succès"), Map.of(
                        "etatCourant", getEtatActuelle().getNom(),
                        "carte", newCarte
                )));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : carte non chargée"));
            }

        }
        catch (XMLFormatException e) {
            GestionnaireException gestionnaireException = new GestionnaireException();
            return gestionnaireException.handleException(e);
        } catch (Exception e) {
            GestionnaireException gestionnaireException = new GestionnaireException();
            return gestionnaireException.handleException(e);
        }
    }

    /**
     * Charge un fichier XML de demande de livraison après qu'une carte a été chargée.
     *
     * @param file fichier XML de la demande
     * @return une {@link ResponseEntity} contenant le résultat du chargement
     */
    @PostMapping("/upload-demande")
    public ResponseEntity<ApiReponse> chargerDemandeLivraison(@RequestParam("file") MultipartFile file) {
        try {
            if (this.carte == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Veuillez d'abord charger une carte avant la demande !"));
            }

            Object result = etatActuelle.chargerDemandeLivraison(this, file, this.carte);

            if (result instanceof DemandeDeLivraison demande) {
                this.demande = demande;
                return ResponseEntity.ok(ApiReponse.succes(("Demande chargée avec succès"), Map.of(
                        "etatCourant", getEtatActuelle().getNom(),
                        "demande", demande
                )));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors du chargement de la demande"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }


    /**
     * Calcule les tournées optimisées pour le nombre de livreurs indiqué.
     *
     * @param nombreLivreurs nombre de livreurs à utiliser
     * @return une {@link ResponseEntity} avec la liste des tournées ou une erreur
     */
    @PostMapping("/tournee/calculer")
    public ResponseEntity<ApiReponse> calculerTournee(@RequestParam(defaultValue = "1") int nombreLivreurs) {
        try {
            Object result = etatActuelle.lancerCalculTournee(this, nombreLivreurs, vitesse);

            if (result instanceof List<?> listeTournees) {
                Map<String, Object> response = new HashMap<>();
                response.put("tournees", listeTournees);
                return ResponseEntity.ok(ApiReponse.succes("Tournées calculées avec succès",response));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur( e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur( "Erreur inconnue"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    /**
     * Génère les feuilles de route au format ZIP pour les tournées calculées.
     *
     * @return un fichier ZIP téléchargeable contenant les feuilles de route
     */
    @PostMapping("/tournee/feuille-de-route")
    public ResponseEntity<?> creerFeuilleDeRoute() {
        try {
            List<Path> fichiers = etatActuelle.creerFeuillesDeRoute(this);
            Path zipPath = Files.createTempFile("feuille-de-route -", ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (Path fichier : fichiers) {
                    ZipEntry entry = new ZipEntry(fichier.getFileName().toString());
                    zos.putNextEntry(entry);
                    Files.copy(fichier, zos);
                    zos.closeEntry();
                }
            }
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(zipPath));
            for (Path fichier : fichiers) {
                Files.deleteIfExists(fichier);
            }
            Files.deleteIfExists(zipPath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"feuilles_de_route.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }

    /**
     * Sauvegarde la tournée calculée sur le disque ou en mémoire.
     *
     * @return une {@link ResponseEntity} indiquant le succès ou l'échec de la sauvegarde
     */
    @PostMapping("/tournee/sauvegarde")
    public ResponseEntity<ApiReponse> sauvegarderTournee() {
        try {
            Object result = etatActuelle.sauvegarderTournee(this);

            if (result instanceof String message) {
                return ResponseEntity.ok(ApiReponse.succes("La tournée a été sauvegardée",Map.of(
                        "message", message
                )));
            } else if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors de la sauvegarde de la tournée."));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception : " + e.getMessage()));
        }
    }

    /**
     * Charge un fichier de tournée sauvegardée.
     *
     * @param file fichier XML ou ZIP contenant les données de tournée
     * @return une {@link ResponseEntity} avec les tournées et la demande associée
     */
    @PostMapping("/upload-tournee")
    public ResponseEntity<ApiReponse> chargerTournee(@RequestParam("file") MultipartFile file) {
        try {
            Object result = etatActuelle.chargerTournee(this, file, this.carte);

            if (result instanceof TourneeUpload upload) {
                return ResponseEntity.ok(ApiReponse.succes("Tournées chargées avec succès", Map.of(
                        "tournees", upload.getTournee(),
                        "demande", upload.getDemande()
                )));
            }

            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur inconnue lors du chargement des tournées"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiReponse.erreur("Erreur serveur : " + e.getMessage()));
        }
    }

    /**
     * Réinitialise l'application en supprimant la carte, la demande et les tournées.
     *
     * @return une {@link ResponseEntity} confirmant la réinitialisation
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiReponse> reinitialiserApplication() {
        try {
            this.carte = null;
            this.demande = null;
            this.etatActuelle = new EtatInitial();

            return ResponseEntity.ok(ApiReponse.succes("Application réinitialisée avec succès.", Map.of(
                    "etatCourant", etatActuelle.getNom()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiReponse.erreur("Erreur lors de la réinitialisation : " + e.getMessage())
            );
        }
    }


    /**
     * Retourne l'état actuel de l'application ainsi que les données chargées.
     *
     * @return une {@link ResponseEntity} contenant l'état courant et les informations de chargement
     */
    @GetMapping("/etat")
    public ResponseEntity<ApiReponse> getEtatActuel() {
        return ResponseEntity.ok(ApiReponse.succes( "Nous sommes dans l'état" + etatActuelle.getNom(), Map.of(
                "etat", etatActuelle.getNom(),
                "carteChargee", carte != null,
                "demandeChargee", demande != null,
                "tourneeChargee", etatActuelle instanceof EtatTourneeCalcule
        )));
    }


    /**
     * Passe en mode modification pour une tournée donnée.
     *
     * @param tourneeCible la tournée à modifier
     * @return une {@link ResponseEntity} confirmant le passage en mode modification
     */
    @PostMapping("/tournee/mode-modification")
    public ResponseEntity<ApiReponse> passerEnModeModification(@RequestBody Tournee tourneeCible) {
        try {
            if (tourneeCible == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Aucune tournée fournie."));
            }

            etatActuelle.passerEnModeModification(this, tourneeCible);

            return ResponseEntity.ok(ApiReponse.succes("Passage en mode modification effectué.", Map.of(
                    "etatCourant", getEtatActuelle().getNom()
            )));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
        }
    }

    /**
     * Modifie une tournée existante en ajoutant ou supprimant une livraison.
     *
     * @param body paramètres JSON de la modification (mode, identifiants, durées)
     * @return une {@link ResponseEntity} contenant la tournée mise à jour
     */
    @PostMapping("/tournee/modifier")
    public ResponseEntity<ApiReponse> modifierTournee(@RequestBody Map<String, Object> body) {
        try {
            String mode = (String) body.get("mode"); // "ajouter" ou "supprimer"

            if (mode == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Le mode doit être précisé (ajouter/supprimer)."));
            }

            Tournee tournee = etatActuelle.modifierTournee(this, mode, body, vitesse);

            return ResponseEntity.ok(ApiReponse.succes("Opération effectuée : " + mode, Map.of(
                    "tournee", tournee,
                    "etatCourant", getEtatActuelle().getNom()
            )));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        } catch (ContrainteDePrecedenceException e ) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiReponse.erreur(e.getMessage()));
        }
    }


    /**
     * Annule la dernière commande effectuée (pattern Command).
     *
     * @return une {@link ResponseEntity} indiquant le résultat de l'annulation
     */
    @PostMapping("/annuler")
    public ResponseEntity<ApiReponse> annuler() {
        try {
            this.annulerCommande();

            // Récupère la tournée actuelle après annulation
            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(ApiReponse.succes("Annulation effectuée", Map.of(
                    "tournee", tourneeActuelle
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    /**
     * Rétablit la dernière commande annulée (pattern Command).
     *
     * @return une {@link ResponseEntity} indiquant le résultat de la restauration
     */
    @PostMapping("/restaurer")
    public ResponseEntity<ApiReponse> restaurer() {
        try {
            this.restaurerCommande();

            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(ApiReponse.succes("Rétablissement effectué", Map.of(
                    "tournee", tourneeActuelle
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    /**
     * Sauvegarde les modifications réalisées sur les tournées et les demandes.
     *
     * @param dto objet contenant les données modifiées à sauvegarder
     * @return une {@link ResponseEntity} avec un message de confirmation
     */
    @PostMapping("/sauvegarder-modifications")
    public ResponseEntity<ApiReponse> sauvegarderModifications(@RequestBody ModificationsDTO dto) {
        try {
            this.demande = dto.getDemande();
            etatActuelle.sauvegarderModification(this, dto.getDemande(), dto.getTournees());
            return ResponseEntity.ok(ApiReponse.succes("Modifications sauvegardées avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur lors de la sauvegarde : " + e.getMessage()));
        }
    }

    /**
     * Exécute une commande et l’ajoute à l’historique pour permettre l’annulation/rétablissement.
     *
     * @param commande la commande à exécuter
     */
    public void executerCommande(Commande commande) {
        historique.executerCommande(commande);
    }

    /**
     * Annule la dernière commande exécutée.
     */
    public void annulerCommande() {
        historique.annuler();
    }

    /**
     * Rétablit la dernière commande annulée.
     */
    public void restaurerCommande() {
        historique.restaurer();
    }

    /**
     * Définit l’état courant de l’application.
     *
     * @param etat le nouvel état
     */
    public void setEtatActuelle(Etat etat) {
        this.etatActuelle = etat;
    }

    /**
     * Retourne l’état courant de l’application.
     *
     * @return l’état actuel
     */
    public Etat getEtatActuelle() {
        return etatActuelle;
    }

    /**
     * Retourne la carte actuellement chargée.
     *
     * @return la {@link Carte} courante
     */
    public Carte getCarte() {
        return carte;
    }

    /**
     * Retourne la demande de livraison actuellement chargée.
     *
     * @return la {@link DemandeDeLivraison} courante
     */
    public DemandeDeLivraison getDemande() {
        return demande;
    }


}
