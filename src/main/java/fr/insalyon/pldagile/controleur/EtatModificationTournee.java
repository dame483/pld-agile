package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
        public Carte loadCarte(Controlleur c, @RequestParam("file") MultipartFile file )
        {
            Carte carte=(Carte)uploadXML("carte", file,this.carte);
            if(carte==null )
            {
                c.setCurrentState(new EtatInitial());
                return carte;
            }

            return carte;
        }

        @Override
        public Object loadDemandeLivraison(Controlleur c, @RequestParam("file")  MultipartFile file, Carte carte) {
            Object dem=uploadXML("demande", file, this.carte);
            if(dem instanceof DemandeDeLivraison){
                c.setCurrentState(new EtatDemandeLivraisonChargee(carte,(DemandeDeLivraison) dem));
                return dem;
            }

            return dem;
        }


        @Override
        public Object runCalculTournee(Controlleur c, int nombreLivreurs, double vitesse) {
            return null;
        }



        @Override
        public Object uploadXML(String type, MultipartFile file, Carte carte) {
            if (file == null || file.isEmpty()) {
                System.err.println("Le fichier est vide ou nul.");
                return null;
            }

            File tempFile = null;
            try {

                tempFile = File.createTempFile(type + "-", ".xml");
                file.transferTo(tempFile);

                System.out.println("Fichier temporaire créé : " + tempFile.getAbsolutePath());

                Object result;

                if ("carte".equalsIgnoreCase(type)) {

                    Carte parsedCarte = CarteParseurXML.loadFromFile(tempFile);
                    result = parsedCarte;
                } else {

                    DemandeDeLivraison parsedDemande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                    System.out.println(parsedDemande);
                    result = parsedDemande;
                }

                return result;

            } catch (Exception e) {
                System.err.println("Erreur lors du chargement XML : " + e.getMessage());
                e.printStackTrace();
                return e;
            } finally {

                if (tempFile != null && tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) {
                        System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
                    }
                }
            }
        }

        @Override
        public Object creerFeuillesDeRoute(Controlleur c) {
            System.err.println("Erreur : impossible de créer une feuille de route en mode modification de la tournée.");
            return null;
        }

        @Override
        public Object saveTournee(Controlleur c) {
            System.err.println("Erreur : impossible de sauvegarder la tournée en mode modification.");
            return null;
        }

        @Override
        public Object loadTournee(Controlleur c, MultipartFile file, Carte carte) {
            Object result = uploadXML("tournee", file, carte);

            if (result instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
                List<Tournee> toutesLesTournees = (List<Tournee>) liste;
                c.setCurrentState(new EtatTourneeCalcule(carte, null, toutesLesTournees));
                return toutesLesTournees;
            }

            return result;
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
                    Long idNoeudPickup = ((Number) body.get("idNoeudPickup")).longValue();
                    Long idNoeudDelivery = ((Number) body.get("idNoeudDelivery")).longValue();

                    commande = new CommandeSuppressionLivraison(
                            tournee,
                            carte,
                            vitesse,
                            idNoeudPickup,
                            idNoeudDelivery
                    );
                }

                case "ajouter" -> {
                    Long idPickup = ((Number) body.get("idNoeudPickup")).longValue();
                    Long idDelivery = ((Number) body.get("idNoeudDelivery")).longValue();
                    Long idPrecedentPickup = ((Number) body.get("idPrecedentPickup")).longValue();
                    Long idPrecedentDelivery = ((Number) body.get("idPrecedentDelivery")).longValue();
                    double dureeEnlevement = (double) body.get("dureeEnlevement");
                    double dureeLivraison = (double) body.get("dureeLivraison");
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




}

