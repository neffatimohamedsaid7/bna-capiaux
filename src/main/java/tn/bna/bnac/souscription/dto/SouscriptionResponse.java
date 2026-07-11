package tn.bna.bnac.souscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vue detaillee d'une souscription (Etape 3 - traitement des souscriptions en cours + detail). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SouscriptionResponse {

    private Long id;
    private String numeroSouscription;
    private String cinRneClient;
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private BigDecimal valeurLiquidative;
    private Integer nombreActionsASouscrire;
    private BigDecimal montantSouscription;
    private String numeroCompteBnaDebit;
    private Integer actionsEnProcessionAvant;
    private LocalDate dateSouscription;
    private LocalDate dateValeurComptable;
    private StatutOperation statut;
    private String referenceWs2;
    private Integer nouveauNombreActions;
}
