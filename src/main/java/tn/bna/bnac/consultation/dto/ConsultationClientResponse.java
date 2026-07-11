package tn.bna.bnac.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.referentiel.dto.ClientBnaDto;
import tn.bna.bnac.ws.dto.ProduitBnacDto;

import java.util.List;

/**
 * Reponse du processus de consultation (section 4.2) : fiche signaletique BNA (si disponible
 * au referentiel) + fiche/portefeuille BNAC. Le fait meme d'obtenir cette reponse signifie
 * que RG4.2 est satisfaite (sinon CompteTitreInexistantException est levee avant).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationClientResponse {

    private ClientBnaDto ficheBna;
    private List<ProduitBnacDto> produits;
}
