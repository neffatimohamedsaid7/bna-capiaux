package tn.bna.bnac.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

import java.math.BigDecimal;

/**
 * Ligne "produit" telle que renvoyee par WS1 (detail client BNAC) :
 * couvre a la fois les besoins d'affichage de la souscription (valeurLiquidativeSouscription)
 * et du rachat (valeurLiquidativeRachat, totaux en cours) pour eviter deux DTO quasi identiques.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitBnacDto {

    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private BigDecimal valeurLiquidativeSouscription;
    private BigDecimal valeurLiquidativeRachat;

    /** Actions en procession : nombre d'actions deja detenues par le client pour ce produit. */
    private Integer nombreActionsEnProcession;

    /** Utilise par le Module 2 (RG2.2). */
    private Integer totalRachatsEnCours;
    private Integer totalRachatsEnAttenteApprobationBnac;
}
