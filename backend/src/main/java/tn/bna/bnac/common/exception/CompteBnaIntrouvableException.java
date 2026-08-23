package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/** Le compte BNA indique (a debiter ou a crediter) n'appartient pas au client recherche. */
public class CompteBnaIntrouvableException extends BusinessException {

    public CompteBnaIntrouvableException(String numeroCompte) {
        super(HttpStatus.BAD_REQUEST,
                "Le compte " + numeroCompte + " n'appartient pas a ce client BNA");
    }
}
