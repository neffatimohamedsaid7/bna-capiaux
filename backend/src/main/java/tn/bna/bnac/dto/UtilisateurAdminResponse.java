package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.Role;

/** Fiche utilisateur pour la gestion des comptes par un ADMIN (sans mot de passe). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurAdminResponse {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private boolean actif;
}
