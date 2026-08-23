package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.Role;

/** Reponse de connexion : token JWT + informations de session pour l'affichage cote frontend. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private String username;
    private String nom;
    private String prenom;
    private Role role;
}
