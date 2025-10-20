package insalyon.pldagile.controleur;

import insalyon.pldagile.controleur.Controlleur;


public interface Etat{
    public void loadCarte(Controlleur c);
    public void loadDemandeLivraison();
    public void addLivraison();
    public void deleteLivraison();
    public void runCalculTournee();
    public void saveTournee();
    /*public void leftClick();
    public void rightClick();*/


}