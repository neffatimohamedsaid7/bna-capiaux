package tn.bna.bnac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Trace de l'ecriture comptable produite a la validation d'une souscription ou d'un rachat
 * (section 1.3/2.3 du cahier des charges : "ecriture comptable : debit compte client / credit
 * compte produit"). Le mouvement reel est effectue par le systeme de core banking BNA, hors
 * perimetre de cette application (aucun WS n'est defini pour cela dans le cahier des charges) ;
 * cette entite ne fait qu'enregistrer, a titre de preuve/tracabilite, les comptes et le montant
 * concernes - au meme titre que {@link JournalAudit}, sans tenue de solde ni grand livre.
 */
@Entity
@Table(name = "ecriture_comptable")
@Getter
@Setter
public class EcritureComptable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_ecriture", nullable = false)
    private Instant dateEcriture;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false, length = 24)
    private TypeOperation typeOperation;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    /** Compte (ou libelle produit) debite. */
    @Column(name = "compte_debit", nullable = false, length = 64)
    private String compteDebit;

    /** Compte (ou libelle produit) credite. */
    @Column(name = "compte_credit", nullable = false, length = 64)
    private String compteCredit;

    @Column(name = "montant", precision = 18, scale = 3, nullable = false)
    private BigDecimal montant;
}
