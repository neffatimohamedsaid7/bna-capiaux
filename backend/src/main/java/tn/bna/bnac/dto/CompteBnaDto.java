package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.TypeCompteBna;

import java.math.BigDecimal;

/** Compte BNA (courant/epargne) du client, tel que fourni par le referentiel BNA. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteBnaDto {

    private String numeroCompte;
    private TypeCompteBna typeCompte;
    private BigDecimal provisionDisponible;
}
