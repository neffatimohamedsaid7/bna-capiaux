package tn.bna.bnac.rachat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.audit.AuditService;
import tn.bna.bnac.comptabilite.EcritureComptableService;
import tn.bna.bnac.common.exception.ClientNonBnaException;
import tn.bna.bnac.common.exception.CompteBnaIntrouvableException;
import tn.bna.bnac.common.exception.DocumentsManquantsException;
import tn.bna.bnac.common.exception.NombreActionsRachatInvalideException;
import tn.bna.bnac.common.exception.OperationIntrouvableException;
import tn.bna.bnac.common.exception.StatutOperationInvalideException;
import tn.bna.bnac.common.storage.DocumentStorageService;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.Rachat;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.domain.TypeAction;
import tn.bna.bnac.domain.TypeDocument;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.NouveauRachatRequest;
import tn.bna.bnac.dto.ProduitRachatDto;
import tn.bna.bnac.dto.RachatResponse;
import tn.bna.bnac.dto.RechercheClientRachatResponse;
import tn.bna.bnac.referentiel.ClientBnaService;
import tn.bna.bnac.dto.ClientBnaDto;
import tn.bna.bnac.repository.DocumentJointRepository;
import tn.bna.bnac.repository.RachatRepository;
import tn.bna.bnac.ws.BnacClient;
import tn.bna.bnac.ws.BnacWebServiceException;
import tn.bna.bnac.dto.ClientBnacDetailResponse;
import tn.bna.bnac.dto.ProduitBnacDto;
import tn.bna.bnac.dto.RachatWsRequest;
import tn.bna.bnac.dto.RachatWsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Module 2 - Demande de rachat d'actions : etapes 1 a 4 (recherche client, nouveau rachat,
 * traitement des rachats en cours, validation) et regles de gestion RG2.1 a RG2.3.
 */
@Service
@RequiredArgsConstructor
public class RachatService {

    private final ClientBnaService clientBnaService;
    private final BnacClient bnacClient;
    private final RachatRepository rachatRepository;
    private final DocumentJointRepository documentJointRepository;
    private final DocumentStorageService documentStorageService;
    private final RachatMapper mapper;
    private final AuditService auditService;
    private final EcritureComptableService ecritureComptableService;

    // ---- Etape 1 : recherche et verification client ----------------------------------------

    public RechercheClientRachatResponse rechercherClient(String critereRecherche) {
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(critereRecherche)
                .orElseThrow(ClientNonBnaException::new);

        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(critereRecherche);

        if (!detailBnac.isPossedeCompteTitre()) {
            return RechercheClientRachatResponse.builder()
                    .ficheBna(ficheBna)
                    .possedeCompteTitre(false)
                    .produits(List.of())
                    .comptesCredit(List.of())
                    .build();
        }

        List<ProduitRachatDto> produits = detailBnac.getProduits().stream()
                .map(this::toProduitRachatDto)
                .toList();

        return RechercheClientRachatResponse.builder()
                .ficheBna(ficheBna)
                .possedeCompteTitre(true)
                .produits(produits)
                // RG2 ne restreint pas le type de compte pour le credit (contrairement a RG1.3 pour le debit).
                .comptesCredit(ficheBna.getComptes())
                .build();
    }

    private ProduitRachatDto toProduitRachatDto(ProduitBnacDto p) {
        return ProduitRachatDto.builder()
                .numeroCompteTitre(p.getNumeroCompteTitre())
                .produit(p.getProduit())
                .valeurLiquidativeRachat(p.getValeurLiquidativeRachat())
                .actionsEnProcession(p.getNombreActionsEnProcession())
                .totalRachatsEnCours(p.getTotalRachatsEnCours())
                .totalRachatsEnAttenteApprobationBnac(p.getTotalRachatsEnAttenteApprobationBnac())
                .build();
    }

    // ---- Etape 2 : nouveau rachat -------------------------------------------------------------

    @Transactional
    public RachatResponse creerRachat(NouveauRachatRequest request) {
        Rachat rachat = new Rachat();
        rachat.setNumeroRachat(genererNumeroRachat());
        renseignerEtValider(rachat, request);
        rachat.setStatut(StatutOperation.EN_COURS_ENREGISTREMENT);
        rachatRepository.save(rachat);
        auditService.enregistrer(TypeOperation.RACHAT, rachat.getId(), TypeAction.CREATION,
                "PEC rachat " + rachat.getNumeroRachat());
        return mapper.toResponse(rachat);
    }

    @Transactional
    public RachatResponse modifierRachat(Long id, NouveauRachatRequest request) {
        Rachat rachat = getOuThrow(id);
        // RG2.1 : une PEC de rachat ne peut etre modifiee que si son statut est "En cours d'enregistrement".
        if (rachat.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(rachat.getStatut());
        }
        renseignerEtValider(rachat, request);
        rachatRepository.save(rachat);
        auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.MODIFICATION,
                "Modification du rachat " + rachat.getNumeroRachat());
        return mapper.toResponse(rachat);
    }

    public void supprimerRachat(Long id) {
        Rachat rachat = getOuThrow(id);
        if (rachat.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(rachat.getStatut());
        }
        rachatRepository.delete(rachat);
        auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.SUPPRESSION,
                "Suppression du rachat " + rachat.getNumeroRachat());
    }

    private void renseignerEtValider(Rachat rachat, NouveauRachatRequest request) {
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(request.getCinRneClient())
                .orElseThrow(ClientNonBnaException::new);

        boolean compteCreditValide = ficheBna.getComptes().stream()
                .anyMatch(c -> c.getNumeroCompte().equals(request.getNumeroCompteBnaCredit()));
        if (!compteCreditValide) {
            throw new CompteBnaIntrouvableException(request.getNumeroCompteBnaCredit());
        }

        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(request.getCinRneClient());
        ProduitBnacDto produitBnac = detailBnac.getProduits().stream()
                .filter(p -> p.getProduit() == request.getProduit()
                        && p.getNumeroCompteTitre().equals(request.getNumeroCompteTitre()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produit " + request.getProduit() + " introuvable pour le compte titre "
                                + request.getNumeroCompteTitre()));

        int actionsEnProcession = produitBnac.getNombreActionsEnProcession() == null
                ? 0 : produitBnac.getNombreActionsEnProcession();
        int totalRachatsEnCours = produitBnac.getTotalRachatsEnCours() == null
                ? 0 : produitBnac.getTotalRachatsEnCours();
        int totalEnAttenteBnac = produitBnac.getTotalRachatsEnAttenteApprobationBnac() == null
                ? 0 : produitBnac.getTotalRachatsEnAttenteApprobationBnac();
        int disponible = actionsEnProcession - totalRachatsEnCours - totalEnAttenteBnac;

        // RG2.2 : nombre d'actions a vendre <= actions en procession - rachats en cours - rachats en attente BNAC.
        if (request.getNombreActionsAVendre() > disponible) {
            throw new NombreActionsRachatInvalideException(request.getNombreActionsAVendre(), disponible);
        }

        BigDecimal montant = produitBnac.getValeurLiquidativeRachat()
                .multiply(BigDecimal.valueOf(request.getNombreActionsAVendre()));

        rachat.setCinRneClient(request.getCinRneClient());
        rachat.setNumeroCompteTitre(request.getNumeroCompteTitre());
        rachat.setProduit(request.getProduit());
        rachat.setValeurLiquidativeRachat(produitBnac.getValeurLiquidativeRachat());
        rachat.setNombreActionsAVendre(request.getNombreActionsAVendre());
        rachat.setMontantRachat(montant);
        rachat.setNumeroCompteBnaCredit(request.getNumeroCompteBnaCredit());
        rachat.setActionsEnProcessionAvantRachat(actionsEnProcession);
        rachat.setTotalRachatsEnCours(totalRachatsEnCours);
        rachat.setTotalRachatsEnAttenteApprobationBnac(totalEnAttenteBnac);
        rachat.setDateRachat(LocalDate.now());
        // RG2.3 : la date valeur comptable est la date de l'operation + 1 jour.
        rachat.setDateValeurComptable(LocalDate.now().plusDays(1));
    }

    // ---- Etape 3 : traitement des rachats en cours ---------------------------------------------

    public List<RachatResponse> listerEnCours(String cinRneClient) {
        return rachatRepository
                .findByCinRneClientAndStatut(cinRneClient, StatutOperation.EN_COURS_ENREGISTREMENT)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public RachatResponse getDetail(Long id) {
        return mapper.toResponse(getOuThrow(id));
    }

    public DocumentJoint importerDocument(Long rachatId, TypeDocument typeDocument, MultipartFile fichier) {
        getOuThrow(rachatId);
        String chemin = documentStorageService.enregistrer(TypeOperation.RACHAT, rachatId, fichier);

        DocumentJoint document = new DocumentJoint();
        document.setTypeOperation(TypeOperation.RACHAT);
        document.setOperationId(rachatId);
        document.setTypeDocument(typeDocument);
        document.setNomFichier(fichier.getOriginalFilename());
        document.setCheminStockage(chemin);
        document.setDateImport(java.time.Instant.now());
        document.setImportePar(auditService.operateurCourant());
        DocumentJoint enregistre = documentJointRepository.save(document);
        auditService.enregistrer(TypeOperation.RACHAT, rachatId, TypeAction.IMPORT_DOCUMENT,
                "Import du document " + typeDocument + " (" + fichier.getOriginalFilename() + ")");
        return enregistre;
    }

    // ---- Tache 2 : validation -------------------------------------------------------------------

    @Transactional
    public RachatResponse valider(Long id) {
        Rachat rachat = getOuThrow(id);
        if (rachat.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(rachat.getStatut());
        }

        List<DocumentJoint> documents = documentJointRepository
                .findByTypeOperationAndOperationId(TypeOperation.RACHAT, id);
        boolean aBulletinSigne = documents.stream()
                .anyMatch(d -> d.getTypeDocument() == TypeDocument.BULLETIN_RACHAT_SIGNE);

        // Etape 4 : import obligatoire = bulletin de rachat signe (client + chef d'agence).
        if (!aBulletinSigne) {
            throw new DocumentsManquantsException("bulletin de rachat signe (client + chef d'agence)");
        }

        RachatWsRequest wsRequest = RachatWsRequest.builder()
                .numeroRachat(rachat.getNumeroRachat())
                .numeroCompteTitre(rachat.getNumeroCompteTitre())
                .produit(rachat.getProduit())
                .nombreActionsAVendre(rachat.getNombreActionsAVendre())
                .valeurLiquidativeRachat(rachat.getValeurLiquidativeRachat())
                .montantRachat(rachat.getMontantRachat())
                .dateValeurComptable(rachat.getDateValeurComptable())
                .build();

        RachatWsResponse wsResponse = bnacClient.demanderRachat(wsRequest);
        if (!wsResponse.isSucces()) {
            auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.APPEL_WS,
                    "WS3 (demande rachat) - echec : " + wsResponse.getMessageErreur());
            throw new BnacWebServiceException("WS3",
                    "BNA Capitaux a refuse la demande de rachat : " + wsResponse.getMessageErreur(), null);
        }
        auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.APPEL_WS,
                "WS3 (demande rachat) - succes, reference " + wsResponse.getReferenceDemandeRachat());

        rachat.setStatut(StatutOperation.VALIDE);
        rachat.setReferenceWs3(wsResponse.getReferenceDemandeRachat());
        rachat.setWs3Succes(true);
        rachatRepository.save(rachat);
        auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.VALIDATION,
                "Rachat " + rachat.getNumeroRachat() + " valide");
        // Symetrique de la souscription (section 2.3) : ecriture comptable debit compte produit / credit compte client.
        ecritureComptableService.enregistrer(TypeOperation.RACHAT, id,
                rachat.getProduit().name(), rachat.getNumeroCompteBnaCredit(),
                rachat.getMontantRachat());
        return mapper.toResponse(rachat);
    }

    public RachatResponse rejeter(Long id) {
        Rachat rachat = getOuThrow(id);
        rachat.setStatut(StatutOperation.REJETE);
        rachatRepository.save(rachat);
        auditService.enregistrer(TypeOperation.RACHAT, id, TypeAction.REJET,
                "Rachat " + rachat.getNumeroRachat() + " rejete");
        return mapper.toResponse(rachat);
    }

    // ---- Utilitaires ----------------------------------------------------------------------------

    private Rachat getOuThrow(Long id) {
        return rachatRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
    }

    private String genererNumeroRachat() {
        return "RACH-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
