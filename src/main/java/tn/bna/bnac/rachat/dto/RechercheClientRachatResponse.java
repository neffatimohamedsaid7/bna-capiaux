package tn.bna.bnac.rachat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.referentiel.dto.ClientBnaDto;
import tn.bna.bnac.referentiel.dto.CompteBnaDto;

import java.util.List;

/**
 * Reponse de l'etape 1 (recherche et verification client) du Module 2.
 * Identique au Module 1 (meme cas de gestion, cf. section 2.2), avec les valeurs
 * liquidatives et totaux specifiques au rachat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheClientRachatResponse {

    private ClientBnaDto ficheBna;
    private boolean possedeCompteTitre;
    private List<ProduitRachatDto> produits;

    /** Comptes BNA du client pouvant recevoir le credit (aucune restriction de type pour le rachat). */
    private List<CompteBnaDto> comptesCredit;
}
