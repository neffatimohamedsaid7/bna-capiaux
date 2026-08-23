package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.EcritureComptable;
import tn.bna.bnac.domain.TypeOperation;

import java.util.List;

public interface EcritureComptableRepository extends JpaRepository<EcritureComptable, Long> {

    List<EcritureComptable> findByTypeOperationAndOperationIdOrderByDateEcritureDescIdDesc(
            TypeOperation typeOperation, Long operationId);
}
