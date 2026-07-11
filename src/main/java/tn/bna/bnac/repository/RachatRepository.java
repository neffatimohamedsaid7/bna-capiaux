package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.Rachat;
import tn.bna.bnac.domain.StatutOperation;

import java.util.List;
import java.util.Optional;

public interface RachatRepository extends JpaRepository<Rachat, Long> {

    Optional<Rachat> findByNumeroRachat(String numeroRachat);

    List<Rachat> findByCinRneClientAndStatut(String cinRneClient, StatutOperation statut);

    /** Utilise pour calculer le "Total rachats en cours" (RG2.2). */
    List<Rachat> findByNumeroCompteTitreAndProduitAndStatut(
            String numeroCompteTitre,
            tn.bna.bnac.domain.ProduitFinancier produit,
            StatutOperation statut);
}
