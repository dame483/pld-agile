package fr.insalyon.pldagile.modele;

public class Livreur {
    private long id;
    private Tournee tournee; // <-- nouvelle référence à sa tournée

    public Livreur(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tournee getTournee() {
        return tournee;
    }

    public void setTournee(Tournee tournee) {
        this.tournee = tournee;
    }

    @Override
    public String toString() {
        return "Livreur{id=" + this.id + ", tournee=" + (tournee != null ? "oui" : "non") + "}";
    }
}
