package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/**
 * RG2.2 : Nombre d'actions a vendre <= (Actions en procession avant rachat
 * - Total rachats en cours - Total rachats en attente d'approbation BNAC).
 */
public class NombreActionsRachatInvalideException extends BusinessException {

    public NombreActionsRachatInvalideException(int demande, int disponible) {
        super(HttpStatus.BAD_REQUEST,
                "Le nombre d'actions a vendre (" + demande + ") depasse le nombre disponible ("
                        + disponible + " = actions en procession - rachats en cours - rachats en attente BNAC)");
    }
}
