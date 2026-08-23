package tn.bna.bnac.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.dto.DashboardStatsResponse;

/**
 * Tableau de bord : compteurs agreges affiches sur la page d'accueil du front
 * (nombre de dossiers en cours d'enregistrement par module).
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableau de bord", description = "Compteurs agreges pour la page d'accueil")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Nombre de dossiers en cours d'enregistrement par module")
    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return dashboardService.getStats();
    }
}
