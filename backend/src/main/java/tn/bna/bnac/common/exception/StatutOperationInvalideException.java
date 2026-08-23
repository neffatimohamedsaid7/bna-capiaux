package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;
import tn.bna.bnac.domain.StatutOperation;

/** RG1.4 / RG2.1 / RG3.2 : une PEC ne peut etre modifiee (ou supprimee) que si son statut est EN_COURS_ENREGISTREMENT. */
public class StatutOperationInvalideException extends BusinessException {

    public StatutOperationInvalideException(StatutOperation statutActuel) {
        super(HttpStatus.CONFLICT,
                "Cette operation ne peut plus etre modifiee : son statut actuel est " + statutActuel
                        + " (seul le statut EN_COURS_ENREGISTREMENT autorise la modification)");
    }
}
