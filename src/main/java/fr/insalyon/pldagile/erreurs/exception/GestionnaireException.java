package fr.insalyon.pldagile.erreurs.exception;

import fr.insalyon.pldagile.sortie.reponse.ApiReponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire global des exceptions pour l'application.
 * Intercepte les exceptions et retourne une réponse API standardisée.
 */
public class GestionnaireException {

    /**
     * Intercepte toutes les exceptions de type {@link Exception} et retourne
     * une réponse HTTP 400 avec un corps contenant l'erreur.
     *
     * @param e L'exception interceptée.
     * @return Une {@link ResponseEntity} contenant un {@link ApiReponse} d'erreur.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiReponse> handleException(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(ApiReponse.erreur(e.getMessage()));
    }
}
