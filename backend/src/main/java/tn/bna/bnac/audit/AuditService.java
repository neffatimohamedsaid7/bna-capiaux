package tn.bna.bnac.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.bna.bnac.domain.JournalAudit;
import tn.bna.bnac.domain.TypeAction;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.AuditEntryResponse;
import tn.bna.bnac.repository.JournalAuditRepository;

import java.time.Instant;
import java.util.List;

/**
 * Journal d'audit (section 6.4 du cahier des charges) : enregistre chaque action significative
 * (PEC, modification, import document, validation, rejet, appel WS) avec l'horodatage et
 * l'identifiant de l'operateur authentifie courant.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final JournalAuditRepository journalAuditRepository;

    public void enregistrer(TypeOperation typeOperation, Long operationId, TypeAction typeAction, String details) {
        JournalAudit entree = new JournalAudit();
        entree.setDateAction(Instant.now());
        entree.setOperateur(operateurCourant());
        entree.setTypeOperation(typeOperation);
        entree.setOperationId(operationId);
        entree.setTypeAction(typeAction);
        entree.setDetails(details);
        journalAuditRepository.save(entree);
    }

    /** Identifiant de l'operateur authentifie courant (meme logique que JpaAuditingConfig.auditorAware). */
    public String operateurCourant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }

    public List<AuditEntryResponse> historique(TypeOperation typeOperation, Long operationId) {
        return journalAuditRepository
                .findByTypeOperationAndOperationIdOrderByDateActionDescIdDesc(typeOperation, operationId)
                .stream()
                .map(e -> AuditEntryResponse.builder()
                        .dateAction(e.getDateAction())
                        .operateur(e.getOperateur())
                        .typeAction(e.getTypeAction())
                        .details(e.getDetails())
                        .build())
                .toList();
    }
}
