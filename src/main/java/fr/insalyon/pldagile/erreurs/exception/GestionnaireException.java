package fr.insalyon.pldagile.erreurs.exception;

import fr.insalyon.pldagile.sortie.reponse.ApiReponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GestionnaireException {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiReponse> handleException(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(ApiReponse.erreur(e.getMessage()));
    }
}
