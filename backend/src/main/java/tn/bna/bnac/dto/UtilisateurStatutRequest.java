package tn.bna.bnac.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Activation / desactivation d'un compte utilisateur par un ADMIN. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurStatutRequest {

    @NotNull(message = "le statut actif est obligatoire")
    private Boolean actif;
}
