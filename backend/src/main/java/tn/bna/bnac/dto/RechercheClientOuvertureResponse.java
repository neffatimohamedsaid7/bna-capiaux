package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Reponse de l'etape 1 (recherche et verification client) du Module 3.
 * Contrairement aux Modules 1 et 2, il n'y a pas de redirection : que le client possede
 * deja un compte titre ou non, il peut demander l'ouverture d'un compte pour un nouveau
 * produit (RG3.3 : si {@code possedeCompteTitre} est faux, les donnees signaletiques BNA
 * seront transmises directement via WS4 a la validation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheClientOuvertureResponse {

    private ClientBnaDto ficheBna;
    private boolean possedeCompteTitre;

    /** Produits deja detenus (vide si le client n'a pas encore de compte titre). */
    private List<ProduitBnacDto> produitsExistants;
}
