//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fr.insalyon.pldagile.modele;

public class Livreur {
    private long id;
    private String nom;
    private double tempsTotalTravail;

    public Livreur(long id, String nom, double tempsTotalTravail) {
        this.id = id;
        this.nom = nom;
        this.tempsTotalTravail = tempsTotalTravail;
    }

    public long getId() {
        return this.id;
    }

    public String getNom() {
        return this.nom;
    }

    public double getTempsTotalTravail() {
        return this.tempsTotalTravail;
    }

    public void setTempsTotalTravail(double tempsTotalTravail) {
        this.tempsTotalTravail = tempsTotalTravail;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return "Livreur{id=" + this.id + ", nom='" + this.nom + "', tempsTotalTravail=" + this.tempsTotalTravail + "}";
    }
}
