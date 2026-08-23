package tn.bna.bnac.souscription;

import org.springframework.stereotype.Service;
import tn.bna.bnac.common.exception.DocumentNonDisponibleException;
import tn.bna.bnac.common.pdf.PdfBulletinBuilder;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.dto.ProduitSouscriptionDto;
import tn.bna.bnac.dto.RechercheClientSouscriptionResponse;
import tn.bna.bnac.dto.SouscriptionResponse;

import java.util.List;

/**
 * Generation des documents PDF du module 1 (section 7.2 du cahier des charges) :
 * liste des produits (Annexe 1, Etape 1), bulletin de souscription (Annexe 2, genere a
 * l'enregistrement) et avis d'operation (genere automatiquement a la validation, Etape 4).
 */
@Service
public class SouscriptionPdfService {

    public byte[] genererListeProduits(RechercheClientSouscriptionResponse r) {
        PdfBulletinBuilder builder = new PdfBulletinBuilder("Liste des produits - Souscription", r.getFicheBna().getCinRne())
                .sansSignatures()
                .section("Client")
                .ligne("Nom et prénom", r.getFicheBna().getNomPrenom())
                .ligne("CIN / RNE", r.getFicheBna().getCinRne());

        List<String[]> lignes = r.getProduits().stream()
                .map(this::ligneProduit)
                .toList();
        builder.tableau(
                new String[]{"N° compte titre", "Produit", "Valeur liquidative", "Actions en procession"},
                lignes);
        return builder.build();
    }

    private String[] ligneProduit(ProduitSouscriptionDto p) {
        return new String[]{
                p.getNumeroCompteTitre(),
                p.getProduit().getLibelle(),
                p.getValeurLiquidativeSouscription() == null ? "-" : p.getValeurLiquidativeSouscription().toString(),
                p.getActionsEnProcession() == null ? "-" : p.getActionsEnProcession().toString()
        };
    }

    public byte[] genererBulletin(SouscriptionResponse s) {
        return new PdfBulletinBuilder("Bulletin de souscription", s.getNumeroSouscription())
                .section("Client")
                .ligne("CIN / RNE", s.getCinRneClient())
                .ligne("N° compte titre", s.getNumeroCompteTitre())
                .section("Souscription")
                .ligne("Produit", s.getProduit().getLibelle())
                .ligne("Valeur liquidative", s.getValeurLiquidative())
                .ligne("Nombre d'actions à souscrire", s.getNombreActionsASouscrire())
                .ligne("Montant de souscription", s.getMontantSouscription())
                .ligne("Compte BNA à débiter", s.getNumeroCompteBnaDebit())
                .ligne("Actions en procession avant souscription", s.getActionsEnProcessionAvant())
                .ligne("Date de souscription", s.getDateSouscription())
                .ligne("Date valeur comptable", s.getDateValeurComptable())
                .ligne("Statut", s.getStatut())
                .build();
    }

    public byte[] genererAvisOperation(SouscriptionResponse s) {
        if (s.getStatut() != StatutOperation.VALIDE) {
            throw new DocumentNonDisponibleException("L'avis d'opération");
        }
        return new PdfBulletinBuilder("Avis d'opération - Souscription", s.getNumeroSouscription())
                .section("Client")
                .ligne("CIN / RNE", s.getCinRneClient())
                .ligne("N° compte titre", s.getNumeroCompteTitre())
                .section("Opération validée")
                .ligne("Produit", s.getProduit().getLibelle())
                .ligne("Nombre d'actions souscrites", s.getNombreActionsASouscrire())
                .ligne("Valeur liquidative", s.getValeurLiquidative())
                .ligne("Montant de souscription", s.getMontantSouscription())
                .ligne("Date valeur comptable", s.getDateValeurComptable())
                .section("Écriture comptable")
                .ligne("Débit compte client", s.getNumeroCompteBnaDebit())
                .ligne("Crédit compte produit", s.getProduit().getLibelle())
                .section("Interfaçage BNAC (WS2)")
                .ligne("Référence souscription", s.getReferenceWs2())
                .ligne("Nouveau nombre d'actions en procession", s.getNouveauNombreActions())
                .build();
    }
}
