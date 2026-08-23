package tn.bna.bnac.souscription;

import org.springframework.stereotype.Component;
import tn.bna.bnac.domain.Souscription;
import tn.bna.bnac.dto.SouscriptionResponse;

@Component
public class SouscriptionMapper {

    public SouscriptionResponse toResponse(Souscription s) {
        return SouscriptionResponse.builder()
                .id(s.getId())
                .numeroSouscription(s.getNumeroSouscription())
                .cinRneClient(s.getCinRneClient())
                .numeroCompteTitre(s.getNumeroCompteTitre())
                .produit(s.getProduit())
                .valeurLiquidative(s.getValeurLiquidative())
                .nombreActionsASouscrire(s.getNombreActionsASouscrire())
                .montantSouscription(s.getMontantSouscription())
                .numeroCompteBnaDebit(s.getNumeroCompteBnaDebit())
                .actionsEnProcessionAvant(s.getActionsEnProcessionAvant())
                .dateSouscription(s.getDateSouscription())
                .dateValeurComptable(s.getDateValeurComptable())
                .statut(s.getStatut())
                .referenceWs2(s.getReferenceWs2())
                .nouveauNombreActions(s.getNouveauNombreActions())
                .build();
    }
}
