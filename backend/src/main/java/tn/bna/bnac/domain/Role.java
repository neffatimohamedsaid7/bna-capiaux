package tn.bna.bnac.domain;

/**
 * Roles applicatifs (section 6.1 du cahier des charges) :
 * - CHARGE_DE_DOSSIER : saisie/PEC des operations (souscription, rachat, ouverture de compte).
 * - VALIDATEUR : validation ou rejet des operations en cours d'enregistrement.
 * - ADMIN : gestion des comptes utilisateurs, acces complet.
 */
public enum Role {
    CHARGE_DE_DOSSIER,
    VALIDATEUR,
    ADMIN
}
