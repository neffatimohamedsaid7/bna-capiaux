package tn.bna.bnac.comptabilite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.EcritureComptableResponse;

import java.util.List;

/** Consultation des ecritures comptables produites a la validation (sections 1.3/2.3 du cahier des charges). */
@RestController
@RequestMapping("/api/ecritures-comptables")
@RequiredArgsConstructor
@Tag(name = "Ecritures comptables", description = "Ecritures debit/credit produites a la validation d'une souscription ou d'un rachat")
public class EcritureComptableController {

    private final EcritureComptableService ecritureComptableService;

    @Operation(summary = "Ecritures comptables d'une operation (souscription ou rachat)")
    @GetMapping("/{typeOperation}/{operationId}")
    public List<EcritureComptableResponse> historique(@PathVariable TypeOperation typeOperation, @PathVariable Long operationId) {
        return ecritureComptableService.historique(typeOperation, operationId);
    }
}
