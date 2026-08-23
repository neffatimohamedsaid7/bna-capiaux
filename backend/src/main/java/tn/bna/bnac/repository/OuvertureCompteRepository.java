package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.OuvertureCompte;
import tn.bna.bnac.domain.StatutOperation;

import java.util.List;
import java.util.Optional;

public interface OuvertureCompteRepository extends JpaRepository<OuvertureCompte, Long> {

    Optional<OuvertureCompte> findByNumeroDemande(String numeroDemande);

    List<OuvertureCompte> findByCinRneClientAndStatut(String cinRneClient, StatutOperation statut);

    /** Tableau de bord : nombre total de dossiers dans un statut donne, tous clients confondus. */
    long countByStatut(StatutOperation statut);
}
