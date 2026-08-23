package tn.bna.bnac.ouverture;

import org.springframework.stereotype.Component;
import tn.bna.bnac.domain.OuvertureCompte;
import tn.bna.bnac.dto.OuvertureCompteResponse;

@Component
public class OuvertureCompteMapper {

    public OuvertureCompteResponse toResponse(OuvertureCompte o) {
        return OuvertureCompteResponse.builder()
                .id(o.getId())
                .numeroDemande(o.getNumeroDemande())
                .cinRneClient(o.getCinRneClient())
                .nomPrenomClient(o.getNomPrenomClient())
                .adresseClient(o.getAdresseClient())
                .activiteClient(o.getActiviteClient())
                .typeCompteSouhaite(o.getTypeCompteSouhaite())
                .dateDemande(o.getDateDemande())
                .statut(o.getStatut())
                .referenceWs4(o.getReferenceWs4())
                .numeroCompteTitreGenere(o.getNumeroCompteTitreGenere())
                .build();
    }
}
