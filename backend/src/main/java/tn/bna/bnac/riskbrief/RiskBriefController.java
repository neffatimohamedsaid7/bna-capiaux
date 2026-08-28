package tn.bna.bnac.riskbrief;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.RiskBriefResponse;

/**
 * Resume de risque genere par IA (fonctionnalite optionnelle, hors perimetre du cahier des
 * charges) pour aider un validateur a revoir une operation. Reserve aux roles qui valident.
 */
@RestController
@RequestMapping("/api/risk-brief")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VALIDATEUR', 'ADMIN')")
@Tag(name = "Resume de risque (IA)", description = "Resume genere par IA pour aider a la validation d'une operation")
public class RiskBriefController {

    private final RiskBriefService riskBriefService;

    @Operation(summary = "Genere un resume de risque pour une operation (souscription, rachat ou ouverture de compte)")
    @GetMapping("/{typeOperation}/{operationId}")
    public RiskBriefResponse genererResume(@PathVariable TypeOperation typeOperation, @PathVariable Long operationId) {
        return riskBriefService.genererResume(typeOperation, operationId);
    }
}
