package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reponse de WS3 (BNAC -> BNA) apres validation d'une demande de rachat. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RachatWsResponse {

    private boolean succes;
    private String messageErreur;
    private String referenceDemandeRachat;
}
