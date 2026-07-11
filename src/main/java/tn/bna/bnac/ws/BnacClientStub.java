package tn.bna.bnac.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.ws.dto.ClientBnacDetailResponse;
import tn.bna.bnac.ws.dto.OuvertureCompteWsRequest;
import tn.bna.bnac.ws.dto.OuvertureCompteWsResponse;
import tn.bna.bnac.ws.dto.ProduitBnacDto;
import tn.bna.bnac.ws.dto.RachatWsRequest;
import tn.bna.bnac.ws.dto.RachatWsResponse;
import tn.bna.bnac.ws.dto.SouscriptionWsRequest;
import tn.bna.bnac.ws.dto.SouscriptionWsResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation simulee de {@link BnacClient}, active par defaut ({@code bnac.ws.stub-mode=true}).
 * Permet de developper et de tester tout le flux PEC/validation sans dependre de la disponibilite
 * reelle de BNA Capitaux. A desactiver des que les vrais endpoints BNAC sont pretes (WS1 a WS4).
 */
@Component
@ConditionalOnProperty(prefix = "bnac.ws", name = "stub-mode", havingValue = "true", matchIfMissing = true)
@Slf4j
public class BnacClientStub implements BnacClient {

    @Override
    public ClientBnacDetailResponse detailClient(String critereRecherche) {
        log.warn("[BNAC STUB] detailClient('{}') - reponse simulee, aucun appel reseau reel", critereRecherche);

        List<ProduitBnacDto> produits = List.of(
                ProduitBnacDto.builder()
                        .numeroCompteTitre("4047/155")
                        .produit(ProduitFinancier.FCP_PROGRES)
                        .valeurLiquidativeSouscription(new BigDecimal("125.500"))
                        .valeurLiquidativeRachat(new BigDecimal("124.900"))
                        .nombreActionsEnProcession(10)
                        .totalRachatsEnCours(0)
                        .totalRachatsEnAttenteApprobationBnac(0)
                        .build(),
                ProduitBnacDto.builder()
                        .numeroCompteTitre("4051/155")
                        .produit(ProduitFinancier.SICAV_BNA)
                        .valeurLiquidativeSouscription(new BigDecimal("98.200"))
                        .valeurLiquidativeRachat(new BigDecimal("97.800"))
                        .nombreActionsEnProcession(0)
                        .totalRachatsEnCours(0)
                        .totalRachatsEnAttenteApprobationBnac(0)
                        .build());

        return ClientBnacDetailResponse.builder()
                .possedeCompteTitre(true)
                .identifiant(critereRecherche)
                .typeIdentifiant("CIN")
                .nomPrenom("Client Simule")
                .adresse("Tunis, Tunisie")
                .activite("Salarie")
                .produits(produits)
                .build();
    }

    @Override
    public SouscriptionWsResponse souscrire(SouscriptionWsRequest request) {
        log.warn("[BNAC STUB] souscrire('{}') - reponse simulee, aucun appel reseau reel",
                request.getNumeroSouscription());
        return SouscriptionWsResponse.builder()
                .succes(true)
                .referenceSouscription("BNAC-SOUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .nouveauNombreActions(request.getNombreActionsASouscrire())
                .build();
    }

    @Override
    public RachatWsResponse demanderRachat(RachatWsRequest request) {
        log.warn("[BNAC STUB] demanderRachat('{}') - reponse simulee, aucun appel reseau reel",
                request.getNumeroRachat());
        return RachatWsResponse.builder()
                .succes(true)
                .referenceDemandeRachat("BNAC-RACH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
    }

    @Override
    public OuvertureCompteWsResponse ouvrirCompte(OuvertureCompteWsRequest request) {
        log.warn("[BNAC STUB] ouvrirCompte('{}') - reponse simulee, aucun appel reseau reel",
                request.getNumeroDemande());
        return OuvertureCompteWsResponse.builder()
                .succes(true)
                .numeroCompteTitre("41" + (int) (Math.random() * 900000 + 100000) + "/155")
                .referenceOuverture("BNAC-OUV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
    }
}
