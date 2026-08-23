package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** L'operation (souscription, rachat, ouverture de compte) demandee n'existe pas. */
public class OperationIntrouvableException extends BusinessException {

    public OperationIntrouvableException(Long id) {
        super(HttpStatus.NOT_FOUND, "Aucune operation trouvee avec l'identifiant " + id);
    }
}
