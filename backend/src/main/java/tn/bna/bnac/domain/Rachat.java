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
import java.time.LocalDate;

/**
 * Module 2 - Demande de rachat d'actions.
 * Couvre la PEC (2.2) et la validation (2.3).
 * Regles de gestion associees : RG2.1 a RG2.3.
 */
@Entity
@Table(name = "rachat")
@Getter
@Setter
public class Rachat extends OperationAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant fonctionnel genere automatiquement (ex: RACH-2026-000045). */
    @Column(name = "numero_rachat", unique = true, length = 32)
    private String numeroRachat;

    @Column(name = "cin_rne_client", length = 32, nullable = false)
    private String cinRneClient;

    @Column(name = "numero_compte_titre", length = 32, nullable = false)
    private String numeroCompteTitre;

    @Enumerated(EnumType.STRING)
    @Column(name = "produit", nullable = false, length = 40)
    private ProduitFinancier produit;

    @Column(name = "valeur_liquidative_rachat", precision = 18, scale = 3, nullable = false)
    private BigDecimal valeurLiquidativeRachat;

    @Column(name = "nombre_actions_a_vendre", nullable = false)
    private Integer nombreActionsAVendre;

    /** Calcule = nombreActionsAVendre * valeurLiquidativeRachat. */
    @Column(name = "montant_rachat", precision = 18, scale = 3, nullable = false)
    private BigDecimal montantRachat;

    /** Le credit sera effectue par BNAC ; ce compte est seulement indique dans la demande. */
    @Column(name = "numero_compte_bna_credit", length = 32, nullable = false)
    private String numeroCompteBnaCredit;

    // --- Elements necessaires au controle RG2.2 (issus de WS1 / agregats BNA) ---
    @Column(name = "actions_en_procession_avant_rachat")
    private Integer actionsEnProcessionAvantRachat;

    @Column(name = "total_rachats_en_cours")
    private Integer totalRachatsEnCours;

    @Column(name = "total_rachats_en_attente_approbation_bnac")
    private Integer totalRachatsEnAttenteApprobationBnac;

    @Column(name = "date_rachat", nullable = false)
    private LocalDate dateRachat;

    /** RG2.3 : la date valeur comptable est la date de l'operation + 1 jour. */
    @Column(name = "date_valeur_comptable")
    private LocalDate dateValeurComptable;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 24)
    private StatutOperation statut = StatutOperation.EN_COURS_ENREGISTREMENT;

    // --- Retour interfacage BNAC (WS3, apres validation) ---
    @Column(name = "reference_ws3")
    private String referenceWs3;

    @Column(name = "ws3_succes")
    private Boolean ws3Succes;
}
