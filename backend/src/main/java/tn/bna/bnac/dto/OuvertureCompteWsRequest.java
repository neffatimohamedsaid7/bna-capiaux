package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

/** Payload envoye a WS4 lors de la validation d'une ouverture de compte titre (BNA -> BNAC). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OuvertureCompteWsRequest {

    private String numeroDemande;
    private String identifiantClient;
    private String typeIdentifiant;
    private String nomPrenom;
    private String adresse;
    private String activite;
    private ProduitFinancier typeCompteSouhaite;
}
