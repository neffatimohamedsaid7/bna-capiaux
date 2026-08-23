package tn.bna.bnac.referentiel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.bna.bnac.domain.TypeCompteBna;
import tn.bna.bnac.dto.ClientBnaDto;
import tn.bna.bnac.dto.CompteBnaDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation simulee du referentiel BNA (core banking), le temps qu'une vraie
 * integration (base BNA / API interne) soit branchee. Deux clients de demonstration
 * sont precharges pour tester le flux complet ; tout autre critere est traite comme
 * "non client BNA" (cas RG1.1 du cahier des charges).
 */
@Service
@Slf4j
public class ClientBnaServiceStub implements ClientBnaService {

    private final Map<String, ClientBnaDto> clientsSimules = Map.of(
            "12345678", ClientBnaDto.builder()
                    .cinRne("12345678")
                    .typePiece("CIN")
                    .numeroPiece("12345678")
                    .nomPrenom("Ahmed Ben Salah")
                    .relation("Client particulier")
                    .activite("Salarie")
                    .adresse("Avenue Habib Bourguiba, Tunis")
                    .comptes(List.of(
                            CompteBnaDto.builder()
                                    .numeroCompte("01100012345")
                                    .typeCompte(TypeCompteBna.TYPE_101)
                                    .provisionDisponible(new BigDecimal("15000.000"))
                                    .build(),
                            CompteBnaDto.builder()
                                    .numeroCompte("01300054321")
                                    .typeCompte(TypeCompteBna.TYPE_103)
                                    .provisionDisponible(new BigDecimal("2500.000"))
                                    .build()))
                    .build(),
            "87654321", ClientBnaDto.builder()
                    .cinRne("87654321")
                    .typePiece("CIN")
                    .numeroPiece("87654321")
                    .nomPrenom("Fatma Trabelsi")
                    .relation("Client particulier")
                    .activite("Profession liberale")
                    .adresse("Rue de Marseille, Sfax")
                    .comptes(List.of(
                            CompteBnaDto.builder()
                                    .numeroCompte("01090099887")
                                    .typeCompte(TypeCompteBna.TYPE_109)
                                    .provisionDisponible(new BigDecimal("500.000"))
                                    .build()))
                    .build(),
            // Client BNA sans compte titre BNAC (utile pour tester le Module 3 - RG3.3).
            "11112222", ClientBnaDto.builder()
                    .cinRne("11112222")
                    .typePiece("CIN")
                    .numeroPiece("11112222")
                    .nomPrenom("Sami Gharbi")
                    .relation("Client particulier")
                    .activite("Commercant")
                    .adresse("Rue Ibn Khaldoun, Sousse")
                    .comptes(List.of(
                            CompteBnaDto.builder()
                                    .numeroCompte("01150011223")
                                    .typeCompte(TypeCompteBna.TYPE_115)
                                    .provisionDisponible(new BigDecimal("8000.000"))
                                    .build()))
                    .build());

    @Override
    public Optional<ClientBnaDto> rechercherClient(String critereRecherche) {
        log.warn("[REFERENTIEL STUB] rechercherClient('{}') - donnees simulees", critereRecherche);
        return Optional.ofNullable(clientsSimules.get(critereRecherche));
    }
}
