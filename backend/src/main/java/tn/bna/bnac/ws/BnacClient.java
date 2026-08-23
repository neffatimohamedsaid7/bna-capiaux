package tn.bna.bnac.ws;

import tn.bna.bnac.dto.ClientBnacDetailResponse;
import tn.bna.bnac.dto.OuvertureCompteWsRequest;
import tn.bna.bnac.dto.OuvertureCompteWsResponse;
import tn.bna.bnac.dto.RachatWsRequest;
import tn.bna.bnac.dto.RachatWsResponse;
import tn.bna.bnac.dto.SouscriptionWsRequest;
import tn.bna.bnac.dto.SouscriptionWsResponse;

/**
 * Contrat d'interfacage avec BNA Capitaux (section 8 du cahier des charges : WS1 a WS4).
 * Deux implementations : {@link BnacRestClient} (appels HTTP reels) et {@link BnacClientStub}
 * (donnees simulees, active par defaut tant que BNAC ne fournit pas d'environnement de test).
 */
public interface BnacClient {

    /** WS1 - Detail client BNAC, declenche a chaque etape "recherche client" (tous modules). */
    ClientBnacDetailResponse detailClient(String critereRecherche);

    /** WS2 - Souscription, declenche a la validation d'une PEC de souscription (Module 1). */
    SouscriptionWsResponse souscrire(SouscriptionWsRequest request);

    /** WS3 - Demande de rachat, declenche a la validation d'une PEC de rachat (Module 2). */
    RachatWsResponse demanderRachat(RachatWsRequest request);

    /** WS4 - Ouverture de compte, declenche a la validation d'une PEC d'ouverture (Module 3). */
    OuvertureCompteWsResponse ouvrirCompte(OuvertureCompteWsRequest request);
}
