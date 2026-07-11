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

import java.time.Instant;

/**
 * Piece jointe importee lors de la PEC ou de la validation (ordre de virement,
 * bulletins signes, formulaire BNAC, CIN, etc.).
 * Rattachee a une operation via (typeOperation, operationId) plutot que via 3 FK
 * distinctes, pour reutiliser le meme modele sur les 3 modules PEC.
 */
@Entity
@Table(name = "document_joint")
@Getter
@Setter
public class DocumentJoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false, length = 24)
    private TypeOperation typeOperation;

    /** Id de la Souscription / Rachat / OuvertureCompte auquel ce document est rattache. */
    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false, length = 40)
    private TypeDocument typeDocument;

    @Column(name = "nom_fichier", nullable = false, length = 256)
    private String nomFichier;

    @Column(name = "chemin_stockage", nullable = false, length = 512)
    private String cheminStockage;

    @Column(name = "date_import", nullable = false)
    private Instant dateImport;

    @Column(name = "importe_par", length = 64)
    private String importePar;
}
