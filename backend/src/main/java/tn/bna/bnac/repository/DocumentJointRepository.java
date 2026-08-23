package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.TypeOperation;

import java.util.List;

public interface DocumentJointRepository extends JpaRepository<DocumentJoint, Long> {

    List<DocumentJoint> findByTypeOperationAndOperationId(TypeOperation typeOperation, Long operationId);
}
