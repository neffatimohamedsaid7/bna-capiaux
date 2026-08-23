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
 * Entree du journal d'audit (section 6.4 du cahier des charges) : une ligne par action
 * significative (PEC, modification, import document, validation, rejet, appel WS), horodatee
 * et rattachee a l'operateur authentifie. Rattachee a une operation via (typeOperation,
 * operationId), comme {@link DocumentJoint}, pour couvrir les 3 modules PEC sans dupliquer le modele.
 */
@Entity
@Table(name = "journal_audit")
@Getter
@Setter
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_action", nullable = false)
    private Instant dateAction;

    @Column(name = "operateur", nullable = false, length = 64)
    private String operateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false, length = 24)
    private TypeOperation typeOperation;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false, length = 24)
    private TypeAction typeAction;

    @Column(name = "details", length = 512)
    private String details;
}
