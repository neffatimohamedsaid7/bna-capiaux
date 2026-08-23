package tn.bna.bnac.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.bna.bnac.dto.DashboardStatsResponse;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.repository.OuvertureCompteRepository;
import tn.bna.bnac.repository.RachatRepository;
import tn.bna.bnac.repository.SouscriptionRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SouscriptionRepository souscriptionRepository;
    private final RachatRepository rachatRepository;
    private final OuvertureCompteRepository ouvertureCompteRepository;

    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .souscriptionsEnCours(souscriptionRepository.countByStatut(StatutOperation.EN_COURS_ENREGISTREMENT))
                .rachatsEnCours(rachatRepository.countByStatut(StatutOperation.EN_COURS_ENREGISTREMENT))
                .ouverturesEnCours(ouvertureCompteRepository.countByStatut(StatutOperation.EN_COURS_ENREGISTREMENT))
                .build();
    }
}
