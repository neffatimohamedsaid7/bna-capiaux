package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** Un compte utilisateur existe deja avec ce nom d'utilisateur. */
public class UtilisateurExistantException extends BusinessException {

    public UtilisateurExistantException(String username) {
        super(HttpStatus.CONFLICT, "Un utilisateur existe deja avec le nom d'utilisateur : " + username);
    }
}
