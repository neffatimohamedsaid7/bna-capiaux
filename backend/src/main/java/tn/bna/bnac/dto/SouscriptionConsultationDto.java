package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vue Souscriptions du Module 4 (section 4.2), une ligne par souscription. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SouscriptionConsultationDto {
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private String idSouscription;
    private LocalDate dateSouscription;
    private Integer actionsASouscrire;
    private BigDecimal valeurLiquidative;
    private BigDecimal montantSouscription;
    private Integer actionsEnProcessionAvant;
    private StatutOperation etatBna;
    /** Derive de ws2Succes : "Approuve", "En attente" ou "-" (pas encore validee cote BNA). */
    private String etatBnac;
    private Integer actionsApresApprobation;
    private String numeroCompte;
    /** Reference utilisable par le front pour retelecharger le bulletin (Annexe 2). */
    private Long idPourEdition;
}
