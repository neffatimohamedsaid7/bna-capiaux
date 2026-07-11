package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** RG1.3 : seuls les comptes de type 101, 103, 109 et 115 sont eligibles a la PEC souscription. */
public class TypeCompteNonEligibleException extends BusinessException {

    public TypeCompteNonEligibleException(String numeroCompte) {
        super(HttpStatus.BAD_REQUEST,
                "Le compte " + numeroCompte + " n'est pas eligible a la souscription "
                        + "(types autorises : 101, 103, 109, 115)");
    }
}
