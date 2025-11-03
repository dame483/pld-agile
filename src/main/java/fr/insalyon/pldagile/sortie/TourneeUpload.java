package fr.insalyon.pldagile.sortie;

public class TourneeUpload {
    private final Object tournee;
    private final Object demande;

    public TourneeUpload(Object tournee, Object demande) {
        this.tournee = tournee;
        this.demande = demande;
    }

    public Object getTournee() {
        return tournee;
    }

    public Object getDemande() {
        return demande;
    }
}