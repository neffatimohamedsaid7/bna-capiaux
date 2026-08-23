package tn.bna.bnac.domain;

/**
 * Nature d'une action tracee dans le journal d'audit (section 6.4 du cahier des charges) :
 * "Toutes les actions (PEC, modification, import document, validation, rejet, appel WS) sont
 * horodatees et enregistrees avec l'identifiant de l'operateur."
 */
public enum TypeAction {
    /** PEC (prise en charge) : creation initiale d'une operation. */
    CREATION,
    MODIFICATION,
    SUPPRESSION,
    IMPORT_DOCUMENT,
    VALIDATION,
    REJET,
    APPEL_WS
}
