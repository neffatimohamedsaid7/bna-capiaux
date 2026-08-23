package tn.bna.bnac.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.AuditEntryResponse;

import java.util.List;

/** Consultation du journal d'audit d'une operation (section 6.4 du cahier des charges). */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Journal d'audit des operations (PEC, modification, import, validation, rejet, appel WS)")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Historique des actions d'une operation (souscription, rachat ou ouverture de compte)")
    @GetMapping("/{typeOperation}/{operationId}")
    public List<AuditEntryResponse> historique(@PathVariable TypeOperation typeOperation, @PathVariable Long operationId) {
        return auditService.historique(typeOperation, operationId);
    }
}
