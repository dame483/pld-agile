package fr.insalyon.pldagile.erreurs.exception;

public class XMLFormatException extends RuntimeException {

    public XMLFormatException(String message) {
        super(message);
    }

    public XMLFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}