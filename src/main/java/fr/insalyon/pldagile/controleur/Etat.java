package fr.insalyon.pldagile.controleur;


interface Etat{
    public void loadCarte();
    public void loadDemandeLivraison();
    public void addLivraison();
    public void deleteLivraison();
    public void runCalculTournee();
    public void saveTournee();
    public void leftClick();
    public void rightClick();


}