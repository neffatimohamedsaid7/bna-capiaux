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

import java.time.LocalDate;

/**
 * Module 3 - Ouverture de compte titre pour les clients BNA sans compte BNAC.
 * Couvre la PEC (3.2) et la validation (3.3).
 * Regles de gestion associees : RG3.1 a RG3.3.
 */
@Entity
@Table(name = "ouverture_compte")
@Getter
@Setter
public class OuvertureCompte extends OperationAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant fonctionnel genere automatiquement (ex: OUV-2026-000012). */
    @Column(name = "numero_demande", unique = true, length = 32)
    private String numeroDemande;

    @Column(name = "cin_rne_client", length = 32, nullable = false)
    private String cinRneClient;

    @Column(name = "nom_prenom_client", length = 128)
    private String nomPrenomClient;

    @Column(name = "adresse_client", length = 256)
    private String adresseClient;

    @Column(name = "activite_client", length = 128)
    private String activiteClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte_souhaite", nullable = false, length = 40)
    private ProduitFinancier typeCompteSouhaite;

    @Column(name = "date_demande", nullable = false)
    private LocalDate dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 24)
    private StatutOperation statut = StatutOperation.EN_COURS_ENREGISTREMENT;

    // --- Retour interfacage BNAC (WS4, apres validation) ---
    @Column(name = "reference_ws4")
    private String referenceWs4;

    @Column(name = "numero_compte_titre_genere", length = 32)
    private String numeroCompteTitreGenere;

    @Column(name = "ws4_succes")
    private Boolean ws4Succes;
}
