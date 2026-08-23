package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.Role;

/** Informations de l'utilisateur courant (endpoint /api/auth/me), sans le mot de passe. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {
    private String username;
    private String nom;
    private String prenom;
    private Role role;
}
