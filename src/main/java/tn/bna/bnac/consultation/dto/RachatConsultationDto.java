package tn.bna.bnac.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vue Rachats du Module 4 (section 4.2), une ligne par rachat. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RachatConsultationDto {
    private String numeroCompteTitre;
    private ProduitFinancier produit;
    private String idRachat;
    private LocalDate dateRachat;
    private Integer actionsAVendre;
    private BigDecimal valeurLiquidative;
    private BigDecimal montantRachat;
    private Integer actionsEnProcessionAvant;
    private StatutOperation etatBna;
    /** Derive de ws3Succes : "Approuve", "En attente" ou "-" (pas encore validee cote BNA). */
    private String etatBnac;
    private String numeroCompte;
    /** Reference utilisable par le front pour retelecharger le bulletin (Annexe 4). */
    private Long idPourEdition;
}
