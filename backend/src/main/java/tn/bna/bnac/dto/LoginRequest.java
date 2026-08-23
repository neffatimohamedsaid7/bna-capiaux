package tn.bna.bnac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Connexion au back-office. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "le mot de passe est obligatoire")
    private String password;
}
