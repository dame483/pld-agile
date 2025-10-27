package fr.insalyon.pldagile.controleur;


import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class EtatAjouterLivraison implements Etat {
    private  Carte carte;
    private DemandeDeLivraison demandeDeLivraison;

    public EtatAjouterLivraison(Carte carte, DemandeDeLivraison demandeDeLivraison) {
        this.carte = carte;
        this.demandeDeLivraison = demandeDeLivraison;
    }

    @Override
    public Carte loadCarte(Controlleur c,@RequestParam("file") MultipartFile file )
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



    /*@Override
    public void addLivraison(Controlleur c,@RequestParam("file")  MultipartFile file, Carte carte) {

    }

    @Override
    public void deleteLivraison(Controlleur c) {

    }

    @Override
    public Object runCalculTournee(Controlleur c) {
        return null;
    }*/

    /*@Override
    public void saveTournee(Controlleur c) {

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
    public String getName() {
        return "Etat Ajout de Livraison";
    }

    @Override
    public Object runCalculTournee(Controlleur c) {
        return null;
    }
}
