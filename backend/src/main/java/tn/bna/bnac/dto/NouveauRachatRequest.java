package tn.bna.bnac.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;

/** Etape 2 - Nouveau rachat : saisie du chargé de dossier. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NouveauRachatRequest {

    @NotBlank(message = "le CIN/RNE du client est obligatoire")
    private String cinRneClient;

    @NotBlank(message = "le numero de compte titre est obligatoire")
    private String numeroCompteTitre;

    @NotNull(message = "le produit est obligatoire")
    private ProduitFinancier produit;

    @NotNull(message = "le nombre d'actions a vendre est obligatoire")
    @Min(value = 1, message = "le nombre d'actions a vendre doit etre superieur a 0")
    private Integer nombreActionsAVendre;

    @NotBlank(message = "le compte BNA a crediter est obligatoire")
    private String numeroCompteBnaCredit;
}
