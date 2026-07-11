package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** RG4.2 : la consultation necessite un compte titre BNAC actif. */
public class CompteTitreInexistantException extends BusinessException {

    public CompteTitreInexistantException() {
        super(HttpStatus.BAD_REQUEST, "Le client ne dispose pas un compte chez BNA capitaux");
    }
}
