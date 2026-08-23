package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload envoye a WS2 lors de la validation d'une souscription (BNA -> BNAC). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SouscriptionWsRequest {

    private String numeroSouscription;
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private Integer nombreActionsASouscrire;
    private BigDecimal valeurLiquidative;
    private BigDecimal montantSouscription;
    private LocalDate dateValeurComptable;
}
