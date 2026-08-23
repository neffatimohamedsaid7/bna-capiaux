package tn.bna.bnac.consultation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.bna.bnac.common.exception.CompteTitreInexistantException;
import tn.bna.bnac.dto.ConsultationClientResponse;
import tn.bna.bnac.dto.RachatConsultationDto;
import tn.bna.bnac.dto.SouscriptionConsultationDto;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.Rachat;
import tn.bna.bnac.domain.Souscription;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.referentiel.ClientBnaService;
import tn.bna.bnac.repository.RachatRepository;
import tn.bna.bnac.repository.SouscriptionRepository;
import tn.bna.bnac.ws.BnacClient;
import tn.bna.bnac.dto.ClientBnacDetailResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Module 4 - Consultation et edition : portefeuille client (RG4.1/RG4.2) et historiques
 * souscriptions/rachats avec filtres (section 4.2).
 */
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ClientBnaService clientBnaService;
    private final BnacClient bnacClient;
    private final SouscriptionRepository souscriptionRepository;
    private final RachatRepository rachatRepository;

    /**
     * RG4.1 : consultable depuis n'importe quelle agence.
     * RG4.2 : necessite uniquement un compte titre BNAC actif (pas de controle "client BNA"
     * explicite dans le cahier des charges pour ce module, contrairement aux Modules 1 a 3).
     */
    public ConsultationClientResponse rechercherPortefeuille(String critereRecherche) {
        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(critereRecherche);
        if (!detailBnac.isPossedeCompteTitre()) {
            throw new CompteTitreInexistantException();
        }

        // Fiche BNA affichee a titre informatif si le referentiel la connait (non bloquant ici).
        var ficheBna = clientBnaService.rechercherClient(critereRecherche).orElse(null);

        return ConsultationClientResponse.builder()
                .ficheBna(ficheBna)
                .produits(detailBnac.getProduits())
                .build();
    }

    public List<SouscriptionConsultationDto> consulterSouscriptions(String critereRecherche,
                                                                      ProduitFinancier produit,
                                                                      StatutOperation etat,
                                                                      LocalDate dateDebut,
                                                                      LocalDate dateFin) {
        return souscriptionRepository.findByCinRneClient(critereRecherche).stream()
                .filter(s -> produit == null || s.getProduit() == produit)
                .filter(s -> etat == null || s.getStatut() == etat)
                .filter(s -> dateDebut == null || !s.getDateSouscription().isBefore(dateDebut))
                .filter(s -> dateFin == null || !s.getDateSouscription().isAfter(dateFin))
                .map(this::toConsultationDto)
                .toList();
    }

    public List<RachatConsultationDto> consulterRachats(String critereRecherche,
                                                          ProduitFinancier produit,
                                                          StatutOperation etat,
                                                          LocalDate dateDebut,
                                                          LocalDate dateFin) {
        return rachatRepository.findByCinRneClient(critereRecherche).stream()
                .filter(r -> produit == null || r.getProduit() == produit)
                .filter(r -> etat == null || r.getStatut() == etat)
                .filter(r -> dateDebut == null || !r.getDateRachat().isBefore(dateDebut))
                .filter(r -> dateFin == null || !r.getDateRachat().isAfter(dateFin))
                .map(this::toConsultationDto)
                .toList();
    }

    private SouscriptionConsultationDto toConsultationDto(Souscription s) {
        return SouscriptionConsultationDto.builder()
                .numeroCompteTitre(s.getNumeroCompteTitre())
                .produit(s.getProduit())
                .idSouscription(s.getNumeroSouscription())
                .dateSouscription(s.getDateSouscription())
                .actionsASouscrire(s.getNombreActionsASouscrire())
                .valeurLiquidative(s.getValeurLiquidative())
                .montantSouscription(s.getMontantSouscription())
                .actionsEnProcessionAvant(s.getActionsEnProcessionAvant())
                .etatBna(s.getStatut())
                .etatBnac(etatBnac(s.getStatut(), s.getWs2Succes()))
                .actionsApresApprobation(s.getNouveauNombreActions())
                .numeroCompte(s.getNumeroCompteBnaDebit())
                .idPourEdition(s.getId())
                .build();
    }

    private RachatConsultationDto toConsultationDto(Rachat r) {
        return RachatConsultationDto.builder()
                .numeroCompteTitre(r.getNumeroCompteTitre())
                .produit(r.getProduit())
                .idRachat(r.getNumeroRachat())
                .dateRachat(r.getDateRachat())
                .actionsAVendre(r.getNombreActionsAVendre())
                .valeurLiquidative(r.getValeurLiquidativeRachat())
                .montantRachat(r.getMontantRachat())
                .actionsEnProcessionAvant(r.getActionsEnProcessionAvantRachat())
                .etatBna(r.getStatut())
                .etatBnac(etatBnac(r.getStatut(), r.getWs3Succes()))
                .numeroCompte(r.getNumeroCompteBnaCredit())
                .idPourEdition(r.getId())
                .build();
    }

    private String etatBnac(StatutOperation statut, Boolean wsSucces) {
        if (statut != StatutOperation.VALIDE) {
            return "-";
        }
        return Boolean.TRUE.equals(wsSucces) ? "Approuve" : "En attente";
    }
}
