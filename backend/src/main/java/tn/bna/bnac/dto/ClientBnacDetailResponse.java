package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Reponse de WS1 - Detail client BNAC.
 * Si le client n'a pas de compte titre, {@code possedeCompteTitre} vaut false et
 * {@code produits} est vide : le module appelant doit alors rediriger vers le Module 3.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientBnacDetailResponse {

    private boolean possedeCompteTitre;

    private String identifiant;
    private String typeIdentifiant;
    private String nomPrenom;
    private String adresse;
    private String activite;

    private List<ProduitBnacDto> produits;
}
