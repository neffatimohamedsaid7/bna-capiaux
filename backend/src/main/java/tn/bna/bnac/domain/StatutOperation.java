package tn.bna.bnac.domain;

/**
 * Statut du cycle de vie d'une operation (souscription, rachat, ouverture de compte titre).
 * Regle commune (RG1.4 / RG2.1 / RG3.2) : une PEC ne peut etre modifiee que si son statut
 * est EN_COURS_ENREGISTREMENT.
 */
public enum StatutOperation {
    EN_COURS_ENREGISTREMENT,
    VALIDE,
    REJETE
}
