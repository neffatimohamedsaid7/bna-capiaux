package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compteurs agreges pour le tableau de bord (page d'accueil du front) : nombre de dossiers
 * en cours d'enregistrement par module, tous clients confondus.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long souscriptionsEnCours;
    private long rachatsEnCours;
    private long ouverturesEnCours;
}
