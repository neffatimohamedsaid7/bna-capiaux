package tn.bna.bnac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bna.bnac.domain.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
