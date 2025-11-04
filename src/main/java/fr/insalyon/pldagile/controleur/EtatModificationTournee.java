package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.exception.XMLFormatException;
import fr.insalyon.pldagile.modele.*;
import fr.insalyon.pldagile.sortie.TourneeUpload;
import fr.insalyon.pldagile.sortie.parseurTourneeJson;
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
        public Object uploadXML(String type, MultipartFile file, Carte carte) throws XMLFormatException {
            if (file == null || file.isEmpty()) {
                throw new XMLFormatException("Le fichier est vide ou nul.");
            }

            File tempFile = null;
            try {
                tempFile = File.createTempFile(type + "-", ".xml");
                file.transferTo(tempFile);

                System.out.println("Fichier temporaire créé : " + tempFile.getAbsolutePath());
                Object result;

                switch (type.toLowerCase()) {
                    case "carte":
                        result = CarteParseurXML.loadFromFile(tempFile);
                        break;

                    case "demande":
                        result = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                        break;

                    case "tournee":
                        Object tournee = parseurTourneeJson.parseurTournee(tempFile.getAbsolutePath());
                        Object demande = parseurTourneeJson.parseurDemandeDeLivraison(tempFile.getAbsolutePath());
                        result = new TourneeUpload(tournee, demande);
                        break;

                    default:
                        throw new XMLFormatException("Type de fichier non reconnu : " + type);
                }

                return result;

            } catch (XMLFormatException e) {
                throw e;

            } catch (Exception e) {
                throw new XMLFormatException("Erreur lors du chargement du fichier XML/JSON : " + e.getMessage(), e);

            } finally {
                if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                    System.err.println("Impossible de supprimer le fichier temporaire : " + tempFile.getAbsolutePath());
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

            if (result instanceof Exception e) {
                return e;
            }

            if (!(result instanceof TourneeUpload upload)) {
                return new Exception("Résultat inattendu lors du chargement de la tournée");
            }

            Object tourneeObj = upload.getTournee();
            Object demandeObj = upload.getDemande();

            DemandeDeLivraison demande;
            if (demandeObj instanceof DemandeDeLivraison d) {
                demande = d;
            } else {
                return new Exception("Objet de demande invalide");
            }

            List<Tournee> toutesLesTournees;
            if (tourneeObj instanceof Tournee tournee) {
                toutesLesTournees = List.of(tournee);
            } else if (tourneeObj instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
                toutesLesTournees = (List<Tournee>) liste;
            } else {
                return new Exception("Fichier JSON invalide ou format incorrect");
            }

            c.setCurrentState(new EtatTourneeCalcule(carte, demande, toutesLesTournees));

            return new TourneeUpload(toutesLesTournees, demande);
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






}

