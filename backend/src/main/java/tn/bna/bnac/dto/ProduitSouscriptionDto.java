package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

import java.math.BigDecimal;

/** Ligne produit affichee lors de la recherche client (Etape 1 / donnees WS1). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitSouscriptionDto {
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private BigDecimal valeurLiquidativeSouscription;
    private Integer actionsEnProcession;
}
