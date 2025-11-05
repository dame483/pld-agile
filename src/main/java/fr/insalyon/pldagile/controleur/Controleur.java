package fr.insalyon.pldagile.controleur;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controleur {

    private Etat etatActuelle;
    private Carte carte;
    private DemandeDeLivraison demande;
    private ListeDeCommandes historique;
    private double vitesse = 4.1;


    public Controleur() {
        this.etatActuelle = new EtatInitial();
        this.historique = new ListeDeCommandes();
    }

    @PostMapping("/upload-carte")
    public ResponseEntity<ApiReponse> loadCarte(@RequestParam("file") MultipartFile file) {
        try {
            Carte newCarte = etatActuelle.loadCarte(this, file);

            if (newCarte != null) {
                this.carte = newCarte;
                this.demande = null;

                return ResponseEntity.ok(ApiReponse.succes(( "Carte chargée avec succès"), Map.of(
                        "etatCourant", getCurrentState().getName(),
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


    @PostMapping("/upload-demande")
    public ResponseEntity<ApiReponse> loadDemandeLivraison(@RequestParam("file") MultipartFile file) {
        try {
            if (this.carte == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Veuillez d'abord charger une carte avant la demande !"));
            }

            Object result = etatActuelle.loadDemandeLivraison(this, file, this.carte);

            if (result instanceof DemandeDeLivraison demande) {
                this.demande = demande; //
                return ResponseEntity.ok(ApiReponse.succes(("Demande chargée avec succès"), Map.of(
                        "etatCourant", getCurrentState().getName(),
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



    @PostMapping("/tournee/calculer")
    public ResponseEntity<ApiReponse> calculerTournee(@RequestParam(defaultValue = "1") int nombreLivreurs) {
        try {
            Object result = etatActuelle.runCalculTournee(this, nombreLivreurs, vitesse);

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

    @PostMapping("/tournee/sauvegarde")
    public ResponseEntity<ApiReponse> saveTournee() {
        try {
            Object result = etatActuelle.saveTournee(this);

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

    @PostMapping("/upload-tournee")
    public ResponseEntity<ApiReponse> uploadTournee(@RequestParam("file") MultipartFile file) {
        try {
            Object result = etatActuelle.loadTournee(this, file, this.carte);

           /* if (result instanceof Exception e) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Exception" + e.getMessage()));
            }*/

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


    @PostMapping("/reset")
    public ResponseEntity<ApiReponse> resetApplication() {
        try {
            this.carte = null;
            this.demande = null;
            this.etatActuelle = new EtatInitial();

            return ResponseEntity.ok(ApiReponse.succes("Application réinitialisée avec succès.", Map.of(
                    "etatCourant", etatActuelle.getName()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiReponse.erreur("Erreur lors de la réinitialisation : " + e.getMessage())
            );
        }
    }


    @GetMapping("/etat")
    public ResponseEntity<ApiReponse> getEtatActuel() {
        return ResponseEntity.ok(ApiReponse.succes( "Nous sommes dans l'état" + etatActuelle.getName(), Map.of(
                "etat", etatActuelle.getName(),
                "carteChargee", carte != null,
                "demandeChargee", demande != null,
                "tourneeChargee", etatActuelle instanceof EtatTourneeCalcule
        )));
    }


    @PostMapping("/tournee/mode-modification")
    public ResponseEntity<ApiReponse> passerEnModeModification(@RequestBody Tournee tourneeCible) {
        try {
            if (tourneeCible == null) {
                return ResponseEntity.badRequest().body(ApiReponse.erreur("Aucune tournée fournie."));
            }

            etatActuelle.passerEnModeModification(this, tourneeCible);

            return ResponseEntity.ok(ApiReponse.succes("Passage en mode modification effectué.", Map.of(
                    "etatCourant", getCurrentState().getName()
            )));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiReponse.erreur("Erreur : " + e.getMessage()));
        }
    }


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
                    "etatCourant", getCurrentState().getName()
            )));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiReponse.erreur(e.getMessage()));
        }
    }





    @PostMapping("/annuler")
    public ResponseEntity<ApiReponse> annuler() {
        try {
            this.annulerCommande();

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

    @PostMapping("/restaurer")
    public ResponseEntity<ApiReponse> restaurer() {
        try {
            this.restaurerCommande();

            Tournee tourneeActuelle = null;
            if (etatActuelle instanceof EtatModificationTournee etatSupp) {
                tourneeActuelle = etatSupp.getTournee();
            }

            return ResponseEntity.ok(ApiReponse.succes("Rétablissement effectué", Map.of(
                    "tournee", tourneeActuelle  // ← Retourne l'état après restauration
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur(e.getMessage()));
        }
    }

    @PostMapping("/sauvegarder-modifications")
    public ResponseEntity<ApiReponse> sauvegarderModifications(@RequestBody ModificationsDTO dto) {
        try {
            etatActuelle.sauvegarderModification(this, dto.getDemande(), dto.getTournees());
            return ResponseEntity.ok(ApiReponse.succes("Modifications sauvegardées avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiReponse.erreur("Erreur lors de la sauvegarde : " + e.getMessage()));
        }
    }

    public void executerCommande(Commande commande) {
        historique.executerCommande(commande);
    }

    public void annulerCommande() {
        historique.annuler();
    }

    public void restaurerCommande() {
        historique.restaurer();
    }

    public void setCurrentState(Etat etat) {
        this.etatActuelle = etat;
    }

    public Etat getCurrentState() {
        return etatActuelle;
    }

    public Carte getCarte() {
        return carte;
    }

    public DemandeDeLivraison getDemande() {
        return demande;
    }


}
