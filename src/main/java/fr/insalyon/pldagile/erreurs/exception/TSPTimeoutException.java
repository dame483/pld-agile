package fr.insalyon.pldagile.erreurs.exception;

/**
 * Exception levée lorsqu'un algorithme TSP dépasse la limite de temps d'exécution.
 * <p>
 * Cette exception est une {@link RuntimeException} et peut être lancée sans être obligatoirement déclarée.
 * Elle signale que le calcul de la tournée optimale (TSP) n'a pas pu être terminé dans le temps imparti.
 */
public class TSPTimeoutException extends RuntimeException {

    /**
     * Crée une nouvelle exception avec un message par défaut.
     */
    public TSPTimeoutException() {
        super("Le calcul du TSP a dépassé le temps maximal autorisé.");
    }

    /**
     * Crée une nouvelle exception avec un message spécifique.
     *
     * @param message Le message décrivant l'erreur.
     */
    public TSPTimeoutException(String message) {
        super(message);
    }

    /**
     * Crée une nouvelle exception avec un message et une cause sous-jacente.
     *
     * @param message Le message décrivant l'erreur.
     * @param cause   La cause originale de l'exception.
     */
    public TSPTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
