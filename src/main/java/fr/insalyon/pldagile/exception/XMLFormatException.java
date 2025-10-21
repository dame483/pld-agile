package fr.insalyon.pldagile.exception;

public class XMLFormatException extends RuntimeException {

    public XMLFormatException(String message) {
        super(message);
    }

    public XMLFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}