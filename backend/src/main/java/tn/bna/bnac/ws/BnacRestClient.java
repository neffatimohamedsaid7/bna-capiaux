package tn.bna.bnac.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.bna.bnac.dto.ClientBnacDetailResponse;
import tn.bna.bnac.dto.OuvertureCompteWsRequest;
import tn.bna.bnac.dto.OuvertureCompteWsResponse;
import tn.bna.bnac.dto.RachatWsRequest;
import tn.bna.bnac.dto.RachatWsResponse;
import tn.bna.bnac.dto.SouscriptionWsRequest;
import tn.bna.bnac.dto.SouscriptionWsResponse;

/**
 * Implementation reelle de {@link BnacClient}, appelant les web services BNAC en HTTP/JSON.
 * Active uniquement quand {@code bnac.ws.stub-mode=false} (cf. application.yml) : a activer
 * une fois les URLs et le format d'echange reels confirmes par BNA Capitaux, la structure
 * exacte des payloads (noms de champs JSON, authentification) restant a caler avec eux.
 */
@Component
@ConditionalOnProperty(prefix = "bnac.ws", name = "stub-mode", havingValue = "false")
@RequiredArgsConstructor
@Slf4j
public class BnacRestClient implements BnacClient {

    private final RestTemplate bnacRestTemplate;
    private final BnacWsProperties properties;

    @Override
    @Cacheable(cacheNames = "detailClientBnac", key = "#critereRecherche")
    public ClientBnacDetailResponse detailClient(String critereRecherche) {
        String url = properties.getBaseUrl() + properties.getWs1DetailClient() + "?critere=" + critereRecherche;
        try {
            return bnacRestTemplate.getForObject(url, ClientBnacDetailResponse.class);
        } catch (RestClientException e) {
            log.error("Echec appel WS1 (detail client BNAC) pour le critere '{}'", critereRecherche, e);
            throw new BnacWebServiceException("WS1", "Impossible de contacter BNA Capitaux (detail client).", e);
        }
    }

    @Override
    public SouscriptionWsResponse souscrire(SouscriptionWsRequest request) {
        String url = properties.getBaseUrl() + properties.getWs2Souscription();
        try {
            return bnacRestTemplate.postForObject(url, request, SouscriptionWsResponse.class);
        } catch (RestClientException e) {
            log.error("Echec appel WS2 (souscription) pour '{}'", request.getNumeroSouscription(), e);
            throw new BnacWebServiceException("WS2", "Impossible de contacter BNA Capitaux (souscription).", e);
        }
    }

    @Override
    public RachatWsResponse demanderRachat(RachatWsRequest request) {
        String url = properties.getBaseUrl() + properties.getWs3DemandeRachat();
        try {
            return bnacRestTemplate.postForObject(url, request, RachatWsResponse.class);
        } catch (RestClientException e) {
            log.error("Echec appel WS3 (demande rachat) pour '{}'", request.getNumeroRachat(), e);
            throw new BnacWebServiceException("WS3", "Impossible de contacter BNA Capitaux (demande de rachat).", e);
        }
    }

    @Override
    public OuvertureCompteWsResponse ouvrirCompte(OuvertureCompteWsRequest request) {
        String url = properties.getBaseUrl() + properties.getWs4OuvertureCompte();
        try {
            return bnacRestTemplate.postForObject(url, request, OuvertureCompteWsResponse.class);
        } catch (RestClientException e) {
            log.error("Echec appel WS4 (ouverture compte) pour '{}'", request.getNumeroDemande(), e);
            throw new BnacWebServiceException("WS4", "Impossible de contacter BNA Capitaux (ouverture de compte).", e);
        }
    }
}
