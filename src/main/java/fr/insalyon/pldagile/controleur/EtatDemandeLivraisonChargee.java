package fr.insalyon.pldagile.controleur;
import fr.insalyon.pldagile.algorithme.CalculTournee;
import fr.insalyon.pldagile.modele.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Component
public class  EtatDemandeLivraisonChargee implements Etat
{
    private final Carte carte;
    private final DemandeDeLivraison demLivraison;
    public EtatDemandeLivraisonChargee(Carte carte,DemandeDeLivraison demande){
            this.carte=carte;
            this.demLivraison=demande;
    }
    @Override
    public Carte loadCarte(Controlleur c,@RequestParam("file") MultipartFile file){
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
        Object dem=uploadXML("demande", file, carte);
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

    }*/

    @Override
    public Object runCalculTournee(Controlleur c)
    {
        CalculTournee t= new CalculTournee(this.carte,this.demLivraison,4.167, this.demLivraison.getEntrepot().getHoraireDepart());
        try {
            Tournee tournee= t.calculerTournee();
            c.setCurrentState(new EtatTourneeCalcule(this.carte, this.demLivraison));
            return tournee;
        } catch (Exception e) {
            return e;
        }

    }

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

                DemandeDeLivraison parsedDemande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, carte);
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


    public String getName(){
        return "Etat Demande de  Livraison Chargee";
    }

}