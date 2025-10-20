package insalyon.pldagile.controleur;

import insalyon.pldagile.controleur.*;



public class Controlleur {
    protected Etat etatActuelle;


    public Controlleur(EtatInitial initial) {
         etatActuelle= initial;
    }

    public void loadCarte() {
            etatActuelle.loadCarte(this);
    }

    public void loadDemandeLivraison(){

    }
    public void addLivraison(){

    }
    public void deleteLivraison(){

    }
    public void runCalculTournee(){

    }
    public void saveTournee(){

    }
    public void leftClick(){

    }
    public void rightClick(){

    }

    protected void SetCurrentState(Etat etat) {
        this.etatActuelle = etat;
    }
}


