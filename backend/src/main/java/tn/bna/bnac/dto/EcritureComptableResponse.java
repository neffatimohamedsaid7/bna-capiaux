package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Une ecriture comptable produite a la validation d'une souscription ou d'un rachat. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcritureComptableResponse {
    private Instant dateEcriture;
    private String compteDebit;
    private String compteCredit;
    private BigDecimal montant;
}
