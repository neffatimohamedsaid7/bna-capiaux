package tn.bna.bnac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.Role;

/** Creation d'un compte utilisateur du back-office par un ADMIN. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurCreateRequest {

    @NotBlank(message = "le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "le mot de passe est obligatoire")
    @Size(min = 8, message = "le mot de passe doit contenir au moins 8 caracteres")
    private String password;

    @NotBlank(message = "le nom est obligatoire")
    private String nom;

    @NotBlank(message = "le prenom est obligatoire")
    private String prenom;

    @Email(message = "email invalide")
    private String email;

    @NotNull(message = "le role est obligatoire")
    private Role role;
}
