package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/** RG1.5 : le montant de souscription ne peut pas depasser la provision du compte BNA choisi. */
public class MontantSuperieurProvisionException extends BusinessException {

    public MontantSuperieurProvisionException(BigDecimal montant, BigDecimal provision) {
        super(HttpStatus.BAD_REQUEST,
                "Le montant de souscription (" + montant + ") depasse la provision disponible "
                        + "du compte (" + provision + ")");
    }
}
