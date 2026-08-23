package tn.bna.bnac.utilisateur;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bna.bnac.common.exception.OperationIntrouvableException;
import tn.bna.bnac.common.exception.UtilisateurExistantException;
import tn.bna.bnac.domain.Utilisateur;
import tn.bna.bnac.dto.UtilisateurAdminResponse;
import tn.bna.bnac.dto.UtilisateurCreateRequest;
import tn.bna.bnac.repository.UtilisateurRepository;

import java.util.List;

/** Gestion des comptes utilisateurs du back-office, reservee au role ADMIN (section 4 du cahier des charges). */
@Service
@RequiredArgsConstructor
public class UtilisateurAdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UtilisateurAdminResponse> lister() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UtilisateurAdminResponse creer(UtilisateurCreateRequest request) {
        if (utilisateurRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UtilisateurExistantException(request.getUsername());
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(request.getUsername());
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setRole(request.getRole());
        utilisateur.setActif(true);

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurAdminResponse changerStatut(Long id, boolean actif) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
        utilisateur.setActif(actif);
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    private UtilisateurAdminResponse toResponse(Utilisateur utilisateur) {
        return UtilisateurAdminResponse.builder()
                .id(utilisateur.getId())
                .username(utilisateur.getUsername())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .actif(utilisateur.isActif())
                .build();
    }
}
