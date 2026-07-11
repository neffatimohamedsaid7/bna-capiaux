package tn.bna.bnac.rachat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

import java.math.BigDecimal;

/** Ligne produit affichee lors de la recherche client pour le rachat (Etape 1 / donnees WS1). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRachatDto {
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private BigDecimal valeurLiquidativeRachat;
    private Integer actionsEnProcession;
    private Integer totalRachatsEnCours;
    private Integer totalRachatsEnAttenteApprobationBnac;
}
