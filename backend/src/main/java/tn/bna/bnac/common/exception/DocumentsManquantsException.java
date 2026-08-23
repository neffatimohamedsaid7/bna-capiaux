package tn.bna.bnac.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Validation d'une souscription/rachat/ouverture sans les pieces jointes obligatoires
 * (ex : ordre de virement + bulletin signe pour la souscription, cf. Etape 4 du Module 1).
 */
public class DocumentsManquantsException extends BusinessException {

    public DocumentsManquantsException(String documentsAttendus) {
        super(HttpStatus.BAD_REQUEST,
                "Import obligatoire avant validation : " + documentsAttendus);
    }
}
