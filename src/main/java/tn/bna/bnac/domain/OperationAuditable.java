package tn.bna.bnac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Socle commun de tracabilite (section 6.4 du cahier des charges) :
 * toutes les operations (PEC, modification, validation, rejet) sont horodatees
 * et enregistrees avec l'identifiant de l'operateur.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class OperationAuditable {

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @LastModifiedDate
    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @CreatedBy
    @Column(name = "cree_par", updatable = false, length = 64)
    private String creePar;

    @LastModifiedBy
    @Column(name = "modifie_par", length = 64)
    private String modifiePar;

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Instant getDateModification() {
        return dateModification;
    }

    public String getCreePar() {
        return creePar;
    }

    public String getModifiePar() {
        return modifiePar;
    }

    public void setModifiePar(String modifiePar) {
        this.modifiePar = modifiePar;
    }
}
