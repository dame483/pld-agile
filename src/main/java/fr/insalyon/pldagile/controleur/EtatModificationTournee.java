package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.algorithme.CalculChemins;
import fr.insalyon.pldagile.algorithme.ModificationTournee;
import fr.insalyon.pldagile.erreurs.exception.ConnexiteDeliveryException;
import fr.insalyon.pldagile.erreurs.exception.ConnexitePickupException;
import fr.insalyon.pldagile.erreurs.exception.ContrainteDePrecedenceException;
import fr.insalyon.pldagile.erreurs.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public Carte chargerCarte(Controleur c, @RequestParam("file") MultipartFile file) {
        throw new IllegalStateException("Erreur : impossible de charger une carte en mode modification.");
    }

    @Override
    public Object chargerDemandeLivraison(Controleur c, @RequestParam("file") MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une demande de livraison en mode modification.");
    }


    @Override
    public Object lancerCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        throw new IllegalStateException("Erreur : impossible de recalculer les tournées en mode modification.");
    }


    @Override
    public Object chargerXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
        throw new IllegalStateException("Erreur : impossible de charger un fichier en mode modification de tournée.");
    }

    @Override
    public List<Path> creerFeuillesDeRoute(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de créer une feuille de route en mode modification de tournée.");
    }

    @Override
    public Object sauvegarderTournee(Controleur c) {
        throw new IllegalStateException("Erreur : impossible de sauvegarder une tournée complète en mode modification.");
    }

    @Override
    public Object chargerTournee(Controleur c, MultipartFile file, Carte carte) {
        throw new IllegalStateException("Erreur : impossible de charger une tournée en mode modification.");
    }

    @Override
    public void passerEnModeModification(Controleur c, Tournee tournee) {
        throw new IllegalStateException("Erreur : Impossible de passer en mode modification à l’état initial.");
    }


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
                boolean connexitePickup = modificationTournee.verifierConnexite(idPickup, idPrecedentPickup);
                boolean connexiteDelivery = modificationTournee.verifierConnexite(idDelivery, idPrecedentDelivery);
                if (!respectContrainte) {
                    throw new ContrainteDePrecedenceException("La contrainte de précédence n'est pas respectée pour le delivery");
                }
                if (!connexitePickup) {
                    throw new ConnexitePickupException("Il n'existe pas de chemins entre le pickup et son précédent");
                }
                if (!connexiteDelivery) {
                    throw  new ConnexiteDeliveryException("Il n'existe pas de chemin entre le délivery et son précédent");
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


    @Override
    public void sauvegarderModification(Controleur c, DemandeDeLivraison demande, List<Tournee> tournees) {
        if (tournees == null || tournees.isEmpty()) {
            throw new IllegalArgumentException("Aucune tournée à sauvegarder.");
        }
        c.setEtatActuelle(new EtatTourneeCalcule(carte, demande, tournees));
    }

    @Override
    public String getNom() {
            return "ModeModificationTournee";
    }

    public Tournee getTournee() {
            return tournee;
    }
}

