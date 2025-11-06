package fr.insalyon.pldagile.sortie;

/**
 * La classe {@code TourneeUpload} représente une structure regroupant
 * une tournée et la demande de livraison associée pour les opérations
 * d'upload ou de transmission vers un service externe.
 *
 * <p>Les objets {@code tournee} et {@code demande} sont ici typés en {@code Object}
 * pour rester génériques, mais peuvent être remplacés par des types plus spécifiques
 * si nécessaire.</p>
 */
public class TourneeUpload {

    /**
     * L'objet représentant la tournée.
     */
    private final Object tournee;

    /**
     * L'objet représentant la demande de livraison associée.
     */
    private final Object demande;

    /**
     * Constructeur de la classe {@code TourneeUpload}.
     *
     * @param tournee l'objet représentant la tournée
     * @param demande l'objet représentant la demande de livraison
     */
    public TourneeUpload(Object tournee, Object demande) {
        this.tournee = tournee;
        this.demande = demande;
    }

    /**
     * Retourne l'objet représentant la tournée.
     *
     * @return l'objet tournée
     */
    public Object getTournee() {
        return tournee;
    }

    /**
     * Retourne l'objet représentant la demande de livraison associée.
     *
     * @return l'objet demande
     */
    public Object getDemande() {
        return demande;
    }
}
