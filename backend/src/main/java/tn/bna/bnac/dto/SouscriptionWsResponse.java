package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reponse de WS2 (BNAC -> BNA) apres validation d'une souscription. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SouscriptionWsResponse {

    private boolean succes;
    private String messageErreur;
    private String referenceSouscription;
    private Integer nouveauNombreActions;
}
