package fr.insalyon.pldagile.erreurs.exception;

/**
 * Exception levée lorsqu'une tournée n'est pas connexe.
 * Cela signifie que certaines livraisons de la tournée ne sont pas accessibles
 * à partir de l'entrepôt ou d'autres points de la tournée.
 */
public class TourneeNonConnexeException extends Exception {

    /**
     * Crée une nouvelle exception avec un message descriptif.
     *
     * @param message Le message expliquant la raison de l'exception.
     */
    public TourneeNonConnexeException(String message) {
        super(message);
    }
}
