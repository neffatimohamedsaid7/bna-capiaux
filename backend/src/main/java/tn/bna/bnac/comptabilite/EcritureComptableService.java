package tn.bna.bnac.comptabilite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.bna.bnac.domain.EcritureComptable;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.EcritureComptableResponse;
import tn.bna.bnac.repository.EcritureComptableRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Ecriture comptable produite a la validation d'une souscription ou d'un rachat (sections 1.3 et
 * 2.3 du cahier des charges). Le mouvement reel (debit/credit) est effectue par le core banking
 * BNA ; cette classe se contente d'en garder une trace consultable, comme {@link
 * tn.bna.bnac.audit.AuditService} le fait pour le journal d'audit.
 */
@Service
@RequiredArgsConstructor
public class EcritureComptableService {

    private final EcritureComptableRepository ecritureComptableRepository;

    public void enregistrer(TypeOperation typeOperation, Long operationId,
                             String compteDebit, String compteCredit, BigDecimal montant) {
        EcritureComptable ecriture = new EcritureComptable();
        ecriture.setDateEcriture(Instant.now());
        ecriture.setTypeOperation(typeOperation);
        ecriture.setOperationId(operationId);
        ecriture.setCompteDebit(compteDebit);
        ecriture.setCompteCredit(compteCredit);
        ecriture.setMontant(montant);
        ecritureComptableRepository.save(ecriture);
    }

    public List<EcritureComptableResponse> historique(TypeOperation typeOperation, Long operationId) {
        return ecritureComptableRepository
                .findByTypeOperationAndOperationIdOrderByDateEcritureDescIdDesc(typeOperation, operationId)
                .stream()
                .map(e -> EcritureComptableResponse.builder()
                        .dateEcriture(e.getDateEcriture())
                        .compteDebit(e.getCompteDebit())
                        .compteCredit(e.getCompteCredit())
                        .montant(e.getMontant())
                        .build())
                .toList();
    }
}
