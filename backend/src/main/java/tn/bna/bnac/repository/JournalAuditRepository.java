package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.JournalAudit;
import tn.bna.bnac.domain.TypeOperation;

import java.util.List;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    // Tri par id (et non uniquement par date) pour garantir l'ordre d'insertion meme si deux actions
    // de la meme requete tombent sur le meme instant (resolution de l'horloge systeme, notamment sous Windows).
    List<JournalAudit> findByTypeOperationAndOperationIdOrderByDateActionDescIdDesc(TypeOperation typeOperation, Long operationId);
}
