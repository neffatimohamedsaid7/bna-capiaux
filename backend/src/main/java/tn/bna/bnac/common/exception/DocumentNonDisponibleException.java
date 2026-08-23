package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** L'avis d'operation / la decharge n'est genere qu'une fois l'operation validee (statut VALIDE). */
public class DocumentNonDisponibleException extends BusinessException {

    public DocumentNonDisponibleException(String document) {
        super(HttpStatus.CONFLICT, document + " n'est disponible qu'apres validation de l'operation");
    }
}
