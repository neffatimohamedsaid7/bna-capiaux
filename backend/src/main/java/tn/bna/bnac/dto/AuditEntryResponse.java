package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.TypeAction;

import java.time.Instant;

/** Une ligne du journal d'audit d'une operation (section 6.4 du cahier des charges). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntryResponse {
    private Instant dateAction;
    private String operateur;
    private TypeAction typeAction;
    private String details;
}
