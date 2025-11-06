package fr.insalyon.pldagile.modele;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Représente un livreur avec un identifiant unique et éventuellement une tournée associée.
 */
public class Livreur {

    /** Identifiant unique du livreur. */
    private long id;

    /** Référence à la tournée du livreur. Ignorée par la sérialisation JSON. */
    @JsonIgnore
    private Tournee tournee;

    /**
     * Constructeur avec l'identifiant du livreur.
     *
     * @param id L'identifiant unique du livreur.
     */
    public Livreur(long id) {
        this.id = id;
    }


    /**
     * Retourne l'identifiant du livreur.
     *
     * @return L'identifiant unique.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Définit l'identifiant du livreur.
     *
     * @param id Le nouvel identifiant à définir.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Retourne la tournée associée au livreur.
     *
     * @return La {@link Tournee} du livreur, ou null si non définie.
     */
    public Tournee getTournee() {
        return tournee;
    }

    /**
     * Définit la tournée associée au livreur.
     *
     * @param tournee La {@link Tournee} à associer au livreur.
     */
    public void setTournee(Tournee tournee) {
        this.tournee = tournee;
    }

    /**
     * Retourne une représentation textuelle du livreur.
     *
     * @return Une chaîne décrivant l'identifiant et si le livreur a une tournée.
     */
    @Override
    public String toString() {
        return "Livreur{id=" + this.id + ", tournee=" + (tournee != null ? "oui" : "non") + "}";
    }
}
