package tn.bna.bnac.referentiel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Fiche signaletique BNA (section "Donnees affichees - Fiche signaletique BNA").
 * Fournie par le referentiel/core banking BNA (hors perimetre BNAC).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientBnaDto {

    private String cinRne;
    private String typePiece;
    private String numeroPiece;
    private String relation;
    private String activite;
    private String adresse;

    private List<CompteBnaDto> comptes;
}
