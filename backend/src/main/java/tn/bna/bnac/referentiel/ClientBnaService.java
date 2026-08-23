package tn.bna.bnac.referentiel;

import tn.bna.bnac.dto.ClientBnaDto;

import java.util.Optional;

/**
 * Acces au referentiel client / comptes BNA (core banking), hors perimetre BNAC.
 * Recherche par CIN, RNE, numero de compte ou numero de titre (section "Etape 1" de chaque module).
 */
public interface ClientBnaService {

    /** Vide si la personne recherchee n'est pas un client BNA (RG1.1 / RG3.1). */
    Optional<ClientBnaDto> rechercherClient(String critereRecherche);
}
