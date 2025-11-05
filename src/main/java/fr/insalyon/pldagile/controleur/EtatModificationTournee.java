package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.TourneeUpload;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class EtatModificationTournee implements Etat {

        private Carte carte;
        private Tournee tournee;

        public EtatModificationTournee(Carte carte, Tournee tournee) {
            this.carte = carte;
            this.tournee = tournee;
        }


    @Override
    public Carte loadCarte(Controlleur c, @RequestParam("file") MultipartFile file) {
        throw new IllegalStateException("Erreur : impossible de charger une carte en mode modification.");
    }

    @Override
    public Object loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une demande de livraison en mode modification.");
    }



    @Override
    public Object runCalculTournee(Controlleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : impossible de recalculer les tournées en mode modification.");
    }



    @Override
    public Object uploadXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        throw new IllegalStateException("Erreur : impossible de charger un fichier en mode modification de tournée.");
    }

    @Override
    public List<Path> creerFeuillesDeRoute(Controlleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route en mode modification de tournée.");
    }

    @Override
    public Object saveTournee(Controlleur c) {
        throw new IllegalStateException("Erreur : impossible de sauvegarder une tournée complète en mode modification.");
    }

    @Override
    public Object loadTournee(Controlleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une tournée en mode modification.");
    }

        @Override
        public void passerEnModeModification(Controlleur c, Tournee tournee){return;}


        @Override
        public String getName() {
            return "ModeModificationTournee";
        }

        public Tournee getTournee() {
            return tournee;
        }



    public void modifierTournee(Controlleur c, String mode, Map<String, Object> body, double vitesse) {
        Commande commande;

        switch (mode.toLowerCase()) {
            case "supprimer" -> {
                long idNoeudPickup = ((Number) body.get("idNoeudPickup")).longValue();
                long idNoeudDelivery = ((Number) body.get("idNoeudDelivery")).longValue();

                commande = new CommandeSuppressionLivraison(
                        tournee,
                        carte,
                        vitesse,
                        idNoeudPickup,
                        idNoeudDelivery
                );
            }

            case "ajouter" -> {
                long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();
                long idPrecedentPickup = ((Number) body.get("idPrecedentPickup")).longValue();
                long idPrecedentDelivery = ((Number) body.get("idPrecedentDelivery")).longValue();
                double dureeEnlevement = ((Number) body.get("dureeEnlevement")).doubleValue();
                double dureeLivraison = ((Number) body.get("dureeLivraison")).doubleValue();

                commande = new CommandeAjoutLivraison(
                        tournee,
                        carte,
                        vitesse,
                        idPickup,
                        idDelivery,
                        idPrecedentPickup,
                        idPrecedentDelivery,
                        dureeEnlevement,
                        dureeLivraison
                );
            }

            default -> throw new IllegalArgumentException("Mode de modification inconnu : " + mode);
        }

        // Exécution de la commande
        c.executerCommande(commande);
    }

    @Override
    public void sauvegarderModification(Controlleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        if (tournees == null || tournees.isEmpty()) {
            throw new IllegalArgumentException("Aucune tournée à sauvegarder.");
        }
        c.setCurrentState(new EtatTourneeCalcule(carte, demande, tournees));
    }
}

