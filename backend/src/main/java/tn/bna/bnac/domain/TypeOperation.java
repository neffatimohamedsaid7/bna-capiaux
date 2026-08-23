package tn.bna.bnac.domain;

/**
 * Type d'operation portee par un document joint (cf. DocumentJoint) :
 * permet de rattacher un document a une Souscription, un Rachat ou une OuvertureCompte
 * sans dupliquer le modele de piece jointe pour chaque module.
 */
public enum TypeOperation {
    SOUSCRIPTION,
    RACHAT,
    OUVERTURE_COMPTE
}
