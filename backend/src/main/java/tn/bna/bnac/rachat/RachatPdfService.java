package tn.bna.bnac.rachat;

import org.springframework.stereotype.Service;
import tn.bna.bnac.common.exception.DocumentNonDisponibleException;
import tn.bna.bnac.common.pdf.PdfBulletinBuilder;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.dto.ProduitRachatDto;
import tn.bna.bnac.dto.RachatResponse;
import tn.bna.bnac.dto.RechercheClientRachatResponse;

import java.util.List;

/**
 * Generation des documents PDF du module 2 (section 7.2 du cahier des charges) :
 * liste des produits rachat (Annexe 3, Etape 1), bulletin de rachat (Annexe 4, genere a
 * l'enregistrement) et decharge (generee automatiquement a la validation, Etape 4).
 */
@Service
public class RachatPdfService {

    public byte[] genererListeProduits(RechercheClientRachatResponse r) {
        PdfBulletinBuilder builder = new PdfBulletinBuilder("Liste des produits - Rachat", r.getFicheBna().getCinRne())
                .sansSignatures()
                .section("Client")
                .ligne("Nom et prénom", r.getFicheBna().getNomPrenom())
                .ligne("CIN / RNE", r.getFicheBna().getCinRne());

        List<String[]> lignes = r.getProduits().stream()
                .map(this::ligneProduit)
                .toList();
        builder.tableau(
                new String[]{"N° compte titre", "Produit", "VL Rachat", "Actions en procession",
                        "Rachats en cours", "En attente BNAC"},
                lignes);
        return builder.build();
    }

    private String[] ligneProduit(ProduitRachatDto p) {
        return new String[]{
                p.getNumeroCompteTitre(),
                p.getProduit().getLibelle(),
                p.getValeurLiquidativeRachat() == null ? "-" : p.getValeurLiquidativeRachat().toString(),
                p.getActionsEnProcession() == null ? "-" : p.getActionsEnProcession().toString(),
                p.getTotalRachatsEnCours() == null ? "-" : p.getTotalRachatsEnCours().toString(),
                p.getTotalRachatsEnAttenteApprobationBnac() == null ? "-" : p.getTotalRachatsEnAttenteApprobationBnac().toString()
        };
    }

    public byte[] genererBulletin(RachatResponse r) {
        return new PdfBulletinBuilder("Bulletin de rachat", r.getNumeroRachat())
                .section("Client")
                .ligne("CIN / RNE", r.getCinRneClient())
                .ligne("N° compte titre", r.getNumeroCompteTitre())
                .section("Rachat")
                .ligne("Produit", r.getProduit().getLibelle())
                .ligne("Valeur liquidative de rachat", r.getValeurLiquidativeRachat())
                .ligne("Nombre d'actions à vendre", r.getNombreActionsAVendre())
                .ligne("Montant de rachat", r.getMontantRachat())
                .ligne("Compte BNA à créditer", r.getNumeroCompteBnaCredit())
                .ligne("Actions en procession avant rachat", r.getActionsEnProcessionAvantRachat())
                .ligne("Date de rachat", r.getDateRachat())
                .ligne("Date valeur comptable", r.getDateValeurComptable())
                .ligne("Statut", r.getStatut())
                .build();
    }

    public byte[] genererDecharge(RachatResponse r) {
        if (r.getStatut() != StatutOperation.VALIDE) {
            throw new DocumentNonDisponibleException("La décharge");
        }
        return new PdfBulletinBuilder("Décharge - Demande de rachat", r.getNumeroRachat())
                .section("Client")
                .ligne("CIN / RNE", r.getCinRneClient())
                .ligne("N° compte titre", r.getNumeroCompteTitre())
                .section("Opération validée")
                .ligne("Produit", r.getProduit().getLibelle())
                .ligne("Nombre d'actions vendues", r.getNombreActionsAVendre())
                .ligne("Valeur liquidative de rachat", r.getValeurLiquidativeRachat())
                .ligne("Montant de rachat", r.getMontantRachat())
                .ligne("Compte BNA crédité (par BNAC)", r.getNumeroCompteBnaCredit())
                .ligne("Date valeur comptable", r.getDateValeurComptable())
                .section("Interfaçage BNAC (WS3)")
                .ligne("Référence demande de rachat", r.getReferenceWs3())
                .build();
    }
}
