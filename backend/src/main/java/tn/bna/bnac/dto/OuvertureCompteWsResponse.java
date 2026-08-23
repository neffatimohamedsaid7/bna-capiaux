package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reponse de WS4 (BNAC -> BNA) apres validation d'une ouverture de compte titre. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OuvertureCompteWsResponse {

    private boolean succes;
    private String messageErreur;
    private String numeroCompteTitre;
    private String referenceOuverture;
}
