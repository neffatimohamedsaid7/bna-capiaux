package tn.bna.bnac.ouverture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

/** Etape 2 - Nouvelle demande d'ouverture de compte titre. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NouvelleOuvertureRequest {

    @NotBlank(message = "le CIN/RNE du client est obligatoire")
    private String cinRneClient;

    @NotNull(message = "le type de compte souhaite est obligatoire")
    private ProduitFinancier typeCompteSouhaite;
}
