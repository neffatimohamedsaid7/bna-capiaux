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
 * Module 1 - Souscription d'actions.
 * Couvre la PEC (1.2) et la validation (1.3) telles que decrites dans le cahier des charges.
 * Regles de gestion associees : RG1.1 a RG1.6.
 */
@Entity
@Table(name = "souscription")
@Getter
@Setter
public class Souscription extends OperationAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant fonctionnel genere automatiquement a l'enregistrement (ex: SOUS-2026-000123). */
    @Column(name = "numero_souscription", unique = true, length = 32)
    private String numeroSouscription;

    // --- Identification client / compte titre (issus de la recherche client + WS1) ---
    @Column(name = "cin_rne_client", length = 32, nullable = false)
    private String cinRneClient;

    @Column(name = "numero_compte_titre", length = 32, nullable = false)
    private String numeroCompteTitre;

    @Enumerated(EnumType.STRING)
    @Column(name = "produit", nullable = false, length = 40)
    private ProduitFinancier produit;

    // --- Donnees de la souscription (Etape 2) ---
    @Column(name = "valeur_liquidative", precision = 18, scale = 3, nullable = false)
    private BigDecimal valeurLiquidative;

    @Column(name = "nombre_actions_a_souscrire", nullable = false)
    private Integer nombreActionsASouscrire;

    /** Calcule = valeurLiquidative * nombreActionsASouscrire. */
    @Column(name = "montant_souscription", precision = 18, scale = 3, nullable = false)
    private BigDecimal montantSouscription;

    @Column(name = "numero_compte_bna_debit", length = 32, nullable = false)
    private String numeroCompteBnaDebit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte_bna_debit", length = 16)
    private TypeCompteBna typeCompteBnaDebit;

    /** Actions deja detenues par le client pour ce produit avant l'operation (issu WS1). */
    @Column(name = "actions_en_procession_avant")
    private Integer actionsEnProcessionAvant;

    @Column(name = "date_souscription", nullable = false)
    private LocalDate dateSouscription;

    /** RG1.6 : la date valeur comptable est la date de l'operation. */
    @Column(name = "date_valeur_comptable")
    private LocalDate dateValeurComptable;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 24)
    private StatutOperation statut = StatutOperation.EN_COURS_ENREGISTREMENT;

    // --- Retour interfacage BNAC (WS2, apres validation) ---
    @Column(name = "reference_ws2")
    private String referenceWs2;

    @Column(name = "nouveau_nombre_actions")
    private Integer nouveauNombreActions;

    @Column(name = "ws2_succes")
    private Boolean ws2Succes;
}
