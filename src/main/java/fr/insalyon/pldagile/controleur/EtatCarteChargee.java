package fr.insalyon.pldagile.controleur;

import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.CarteParseurXML;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import fr.insalyon.pldagile.modele.DemandeDeLivraisonParseurXML;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Component
public class  EtatCarteChargee implements Etat
{
    private final Carte carte;
    public EtatCarteChargee(Carte c)
    {
        this.carte=c;

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
    public DemandeDeLivraison loadDemandeLivraison(Controlleur c, @RequestParam("file") MultipartFile file, Carte carte )
    {
        DemandeDeLivraison dem=(DemandeDeLivraison)uploadXML("demande", file, this.carte);
        if(dem!=null ){
            c.setCurrentState(new EtatDemandeLivraisonChargee(carte,dem));
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
    public void runCalculTournee(Controlleur c) {

    }

    /*@Override
    public void saveTournee(Controlleur c) {

    }*/


    @Override
    public Object uploadXML(String type, @RequestParam("file") MultipartFile file,Carte carte ){

        try {
            if (file.isEmpty()) {
                return null;
            } else {
                File tempFile = File.createTempFile(type+"-", ".xml");
                file.transferTo(tempFile);
                if(type.equals("carte")) {
                    carte = CarteParseurXML.loadFromFile(tempFile);
                    tempFile.delete();
                    return carte;
                }
                else{
                    DemandeDeLivraison demande;
                    try {
                        file.transferTo(tempFile);
                        demande = DemandeDeLivraisonParseurXML.loadFromFile(tempFile, this.carte);
                    } finally {
                        tempFile.delete();
                    }

                    return demande;

                }

            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Etat Carte Chargee";
    }


}