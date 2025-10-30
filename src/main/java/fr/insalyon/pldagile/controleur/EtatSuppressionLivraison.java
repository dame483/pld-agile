package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public class EtatSuppressionLivraison implements Etat{

    private Carte carte;
    private Tournee tournee;

    public EtatSuppressionLivraison(Carte carte, Tournee tournee) {
        this.carte = carte;
        this.tournee = tournee;
    }


    @Override
    public Carte loadCarte(Controleur c, @RequestParam("file") MultipartFile file )
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
    public Object loadDemandeLivraison(Controleur c, @RequestParam("file")  MultipartFile file, Carte carte) {
        Object dem=uploadXML("demande", file, this.carte);
        if(dem instanceof DemandeDeLivraison){
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte,(DemandeDeLivraison) dem));
            return dem;
        }

        return dem;
    }


    @Override
    public Object runCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
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
    public Object creerFeuillesDeRoute(Controleur c) {
        System.err.println("Erreur : impossible de créer une feuille de route en mode modification de la tournée.");
        return null;
    }

    @Override
    public Object saveTournee(Controleur c) {
        System.err.println("Erreur : impossible de sauvegarder la tournée en mode modification.");
        return null;
    }

    @Override
    public Object loadTournee(Controleur c, MultipartFile file, Carte carte) {
        Object result = uploadXML("tournee", file, carte);

        if (result instanceof List<?> liste && !liste.isEmpty() && liste.get(0) instanceof Tournee) {
            List<Tournee> toutesLesTournees = (List<Tournee>) liste;
            c.setCurrentState(new EtatTourneeCalcule(carte, null, toutesLesTournees));
            return toutesLesTournees;
        }

        return result;
    }

    @Override
    public void passerEnModeSuppression(Controleur c, Tournee tournee){return;}


    @Override
    public String getName() {
        return "Etat Supression de Livraison";
    }

    public Tournee getTournee() {
        return tournee;
    }

    public void supprimmerLivraison(Controleur c, Long idNoeudClique, Long idNoeudAssocie, double vitesse) {
        if (idNoeudAssocie == null || idNoeudClique == null) {
            System.err.println("Erreur : un des nœuds est null.");
            return;
        }

        // Création de la commande de suppression
        Commande commandeSuppression = new CommandeSuppressionLivraison(
                tournee,// la tournée sur laquelle on supprime
                carte,
                vitesse,
                idNoeudClique,  // nœud sélectionné sur la carte (pickup ou delivery)
                idNoeudAssocie  // le nœud associé correspondant
        );

        // Exécution de la commande via le contrôleur (gestion de l'historique)
        c.executerCommande(commandeSuppression);

        // Affichage console pour debug
        System.out.println("Livraison supprimée : " + idNoeudClique + " et " + idNoeudAssocie);
    }





}