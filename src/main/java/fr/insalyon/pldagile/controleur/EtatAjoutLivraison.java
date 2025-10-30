package fr.insalyon.pldagile.controleur;


import fr.insalyon.pldagile.modele.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public class EtatAjoutLivraison implements Etat {
    private  Carte carte;
    private DemandeDeLivraison demande;

    public EtatAjoutLivraison(Carte carte, DemandeDeLivraison demandeDeLivraison) {
        this.carte = carte;
        this.demande = demandeDeLivraison;
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

    public Object passerEnModeSuppression(Controleur c){
        return null;
    }



    /*@Override
    public void addLivraison(Controleur c,@RequestParam("file")  MultipartFile file, Carte carte) {

    }

    @Override
    public void deleteLivraison(Controleur c) {

    }

    @Override
    public Object runCalculTournee(Controleur c) {
        return null;
    }*/

    /*@Override
    public void saveTournee(Controleur c) {

    }*/


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
    public void passerEnModeSuppression(Controleur c, Tournee tournee){return;}

    @Override
    public String getName() {
        return "Etat Ajout de Livraison";
    }

    @Override
    public Object runCalculTournee(Controleur c, int nombreLivreurs, double vitesse) {
        return null;
    }
}