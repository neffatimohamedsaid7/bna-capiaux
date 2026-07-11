package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base des exceptions representant une regle de gestion violee (RG1.x, RG2.x, RG3.x, ...).
 * Chaque sous-classe porte le statut HTTP le plus adapte et un message destine a etre
 * affiche tel quel cote agence (cf. messages bloquants du cahier des charges).
 */
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;

    protected BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
