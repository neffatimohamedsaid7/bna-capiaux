package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.Souscription;
import tn.bna.bnac.domain.StatutOperation;

import java.util.List;
import java.util.Optional;

public interface SouscriptionRepository extends JpaRepository<Souscription, Long> {

    Optional<Souscription> findByNumeroSouscription(String numeroSouscription);

    /** Etape 3 : souscriptions en cours d'enregistrement liees a un client (par CIN/RNE). */
    List<Souscription> findByCinRneClientAndStatut(String cinRneClient, StatutOperation statut);

    List<Souscription> findByNumeroCompteTitreAndStatut(String numeroCompteTitre, StatutOperation statut);

    /** Module 4 - Consultation : historique complet (tous statuts) pour un client. */
    List<Souscription> findByCinRneClient(String cinRneClient);
}
