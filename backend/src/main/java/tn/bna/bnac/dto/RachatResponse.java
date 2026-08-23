package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vue detaillee d'un rachat (Etape 3 - traitement des rachats en cours + detail). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RachatResponse {

    private Long id;
    private String numeroRachat;
    private String cinRneClient;
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private BigDecimal valeurLiquidativeRachat;
    private Integer nombreActionsAVendre;
    private BigDecimal montantRachat;
    private String numeroCompteBnaCredit;
    private Integer actionsEnProcessionAvantRachat;
    private LocalDate dateRachat;
    private LocalDate dateValeurComptable;
    private StatutOperation statut;
    private String referenceWs3;
}
