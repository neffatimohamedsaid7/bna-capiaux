package tn.bna.bnac.domain;

/**
 * Produits financiers proposes par BNA Capitaux (section 2 - Terminologie du cahier des charges).
 */
public enum ProduitFinancier {
    FCP_PROGRES("FCP Progrès"),
    FCP_BNA_CAPITALISATION("FCP BNA Capitalisation"),
    PLACEMENT_OBLIGATOIRE_SICAV("Placement obligatoire SICAV"),
    SICAV("SICAV"),
    SICAV_BNA("SICAV BNA");

    private final String libelle;

    ProduitFinancier(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
