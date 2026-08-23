package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** RG1.1 / RG3.1 : seuls les clients BNA sont eligibles. */
public class ClientNonBnaException extends BusinessException {

    public ClientNonBnaException() {
        super(HttpStatus.BAD_REQUEST, "La personne recherchée n'est pas un client BNA");
    }
}
