package tn.bna.bnac.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload envoye a WS3 lors de la validation d'une demande de rachat (BNA -> BNAC). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RachatWsRequest {

    private String numeroRachat;
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private Integer nombreActionsAVendre;
    private BigDecimal valeurLiquidativeRachat;
    private BigDecimal montantRachat;
    private LocalDate dateValeurComptable;
}
