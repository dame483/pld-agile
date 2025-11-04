package fr.insalyon.pldagile.sortie.reponse;

public record ApiReponse(
        boolean success,
        String message,
        Object data
) {
    public static ApiReponse succes(String message, Object data) {
        return new ApiReponse(true, message, data);
    }

    public static ApiReponse erreur(String message) {
        return new ApiReponse(false, message, null);
    }
}