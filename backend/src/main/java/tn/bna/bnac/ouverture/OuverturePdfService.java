package tn.bna.bnac.ouverture;

import org.springframework.stereotype.Service;
import tn.bna.bnac.common.pdf.PdfBulletinBuilder;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.dto.OuvertureCompteResponse;

/**
 * Generation du bulletin d'ouverture de compte titre (module 3, section 7.2 du cahier des charges),
 * genere automatiquement a l'enregistrement de la demande (Etape 2) et complete par la reference
 * BNAC une fois la demande validee (Etape 4, WS4).
 */
@Service
public class OuverturePdfService {

    public byte[] genererBulletin(OuvertureCompteResponse d) {
        PdfBulletinBuilder builder = new PdfBulletinBuilder("Bulletin d'ouverture de compte titre", d.getNumeroDemande())
                .section("Client")
                .ligne("CIN / RNE", d.getCinRneClient())
                .ligne("Nom et prénom", d.getNomPrenomClient())
                .ligne("Adresse", d.getAdresseClient())
                .ligne("Activité", d.getActiviteClient())
                .section("Demande d'ouverture")
                .ligne("Type de compte souhaité", d.getTypeCompteSouhaite().getLibelle())
                .ligne("Date de la demande", d.getDateDemande())
                .ligne("Statut", d.getStatut());

        if (d.getStatut() == StatutOperation.VALIDE) {
            builder.section("Interfaçage BNAC (WS4)")
                    .ligne("Référence ouverture de compte", d.getReferenceWs4())
                    .ligne("N° compte titre généré", d.getNumeroCompteTitreGenere());
        }

        return builder.build();
    }
}
