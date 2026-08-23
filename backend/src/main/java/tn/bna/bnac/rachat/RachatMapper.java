package tn.bna.bnac.rachat;

import org.springframework.stereotype.Component;
import tn.bna.bnac.domain.Rachat;
import tn.bna.bnac.dto.RachatResponse;

@Component
public class RachatMapper {

    public RachatResponse toResponse(Rachat r) {
        return RachatResponse.builder()
                .id(r.getId())
                .numeroRachat(r.getNumeroRachat())
                .cinRneClient(r.getCinRneClient())
                .numeroCompteTitre(r.getNumeroCompteTitre())
                .produit(r.getProduit())
                .valeurLiquidativeRachat(r.getValeurLiquidativeRachat())
                .nombreActionsAVendre(r.getNombreActionsAVendre())
                .montantRachat(r.getMontantRachat())
                .numeroCompteBnaCredit(r.getNumeroCompteBnaCredit())
                .actionsEnProcessionAvantRachat(r.getActionsEnProcessionAvantRachat())
                .dateRachat(r.getDateRachat())
                .dateValeurComptable(r.getDateValeurComptable())
                .statut(r.getStatut())
                .referenceWs3(r.getReferenceWs3())
                .build();
    }
}
