package tn.bna.bnac.souscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.referentiel.dto.ClientBnaDto;
import tn.bna.bnac.referentiel.dto.CompteBnaDto;

import java.util.List;

/**
 * Reponse de l'etape 1 (recherche et verification client) du Module 1.
 * Si {@code possedeCompteTitre} est faux, le front doit rediriger vers le Module 3
 * (ouverture de compte titre) - {@code produits} et {@code comptesEligiblesDebit} sont alors vides.
 * Le cas "client non BNA" n'apparait pas ici : il remonte en erreur 400 (voir ClientNonBnaException).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheClientSouscriptionResponse {

    private ClientBnaDto ficheBna;
    private boolean possedeCompteTitre;
    private List<ProduitSouscriptionDto> produits;

    /** Comptes BNA du client eligibles au debit (types 101, 103, 109, 115 - RG1.3). */
    private List<CompteBnaDto> comptesEligiblesDebit;
}
