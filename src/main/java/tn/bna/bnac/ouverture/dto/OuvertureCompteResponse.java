package tn.bna.bnac.ouverture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.time.LocalDate;

/** Vue detaillee d'une demande d'ouverture de compte titre (Etape 3 + detail). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OuvertureCompteResponse {

    private Long id;
    private String numeroDemande;
    private String cinRneClient;
    private String nomPrenomClient;
    private String adresseClient;
    private String activiteClient;
    private ProduitFinancier typeCompteSouhaite;
    private LocalDate dateDemande;
    private StatutOperation statut;
    private String referenceWs4;
    private String numeroCompteTitreGenere;
}
