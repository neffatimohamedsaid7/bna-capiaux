package tn.bna.bnac.souscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.audit.AuditService;
import tn.bna.bnac.comptabilite.EcritureComptableService;
import tn.bna.bnac.common.exception.ClientNonBnaException;
import tn.bna.bnac.common.exception.DocumentsManquantsException;
import tn.bna.bnac.common.exception.MontantSuperieurProvisionException;
import tn.bna.bnac.common.exception.OperationIntrouvableException;
import tn.bna.bnac.common.exception.StatutOperationInvalideException;
import tn.bna.bnac.common.exception.TypeCompteNonEligibleException;
import tn.bna.bnac.common.storage.DocumentStorageService;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.Souscription;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.domain.TypeAction;
import tn.bna.bnac.domain.TypeCompteBna;
import tn.bna.bnac.domain.TypeDocument;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.referentiel.ClientBnaService;
import tn.bna.bnac.dto.ClientBnaDto;
import tn.bna.bnac.dto.CompteBnaDto;
import tn.bna.bnac.repository.DocumentJointRepository;
import tn.bna.bnac.repository.SouscriptionRepository;
import tn.bna.bnac.dto.NouvelleSouscriptionRequest;
import tn.bna.bnac.dto.ProduitSouscriptionDto;
import tn.bna.bnac.dto.RechercheClientSouscriptionResponse;
import tn.bna.bnac.dto.SouscriptionResponse;
import tn.bna.bnac.ws.BnacClient;
import tn.bna.bnac.ws.BnacWebServiceException;
import tn.bna.bnac.dto.ClientBnacDetailResponse;
import tn.bna.bnac.dto.ProduitBnacDto;
import tn.bna.bnac.dto.SouscriptionWsRequest;
import tn.bna.bnac.dto.SouscriptionWsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Module 1 - Souscription d'actions : implemente les etapes 1 a 4 (recherche client,
 * nouvelle souscription, traitement des souscriptions en cours, validation) et les
 * regles de gestion RG1.1 a RG1.6.
 */
@Service
@RequiredArgsConstructor
public class SouscriptionService {

    private static final Set<TypeCompteBna> TYPES_COMPTE_ELIGIBLES = Set.of(
            TypeCompteBna.TYPE_101, TypeCompteBna.TYPE_103, TypeCompteBna.TYPE_109, TypeCompteBna.TYPE_115);

    private final ClientBnaService clientBnaService;
    private final BnacClient bnacClient;
    private final SouscriptionRepository souscriptionRepository;
    private final DocumentJointRepository documentJointRepository;
    private final DocumentStorageService documentStorageService;
    private final SouscriptionMapper mapper;
    private final AuditService auditService;
    private final EcritureComptableService ecritureComptableService;

    // ---- Etape 1 : recherche et verification client ----------------------------------------

    public RechercheClientSouscriptionResponse rechercherClient(String critereRecherche) {
        // RG1.1 : seuls les clients BNA sont eligibles.
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(critereRecherche)
                .orElseThrow(ClientNonBnaException::new);

        // RG1.2 : la PEC de souscription est reservee aux clients disposant deja d'un compte titre BNAC.
        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(critereRecherche);

        if (!detailBnac.isPossedeCompteTitre()) {
            return RechercheClientSouscriptionResponse.builder()
                    .ficheBna(ficheBna)
                    .possedeCompteTitre(false)
                    .produits(List.of())
                    .comptesEligiblesDebit(List.of())
                    .build();
        }

        List<ProduitSouscriptionDto> produits = detailBnac.getProduits().stream()
                .map(this::toProduitSouscriptionDto)
                .toList();

        List<CompteBnaDto> comptesEligibles = ficheBna.getComptes().stream()
                .filter(c -> TYPES_COMPTE_ELIGIBLES.contains(c.getTypeCompte()))
                .toList();

        return RechercheClientSouscriptionResponse.builder()
                .ficheBna(ficheBna)
                .possedeCompteTitre(true)
                .produits(produits)
                .comptesEligiblesDebit(comptesEligibles)
                .build();
    }

    private ProduitSouscriptionDto toProduitSouscriptionDto(ProduitBnacDto p) {
        return ProduitSouscriptionDto.builder()
                .numeroCompteTitre(p.getNumeroCompteTitre())
                .produit(p.getProduit())
                .valeurLiquidativeSouscription(p.getValeurLiquidativeSouscription())
                .actionsEnProcession(p.getNombreActionsEnProcession())
                .build();
    }

    // ---- Etape 2 : nouvelle souscription -----------------------------------------------------

    @Transactional
    public SouscriptionResponse creerSouscription(NouvelleSouscriptionRequest request) {
        Souscription souscription = new Souscription();
        souscription.setNumeroSouscription(genererNumeroSouscription());
        renseignerEtValider(souscription, request);
        souscription.setStatut(StatutOperation.EN_COURS_ENREGISTREMENT);
        souscriptionRepository.save(souscription);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, souscription.getId(), TypeAction.CREATION,
                "PEC souscription " + souscription.getNumeroSouscription());
        return mapper.toResponse(souscription);
    }

    @Transactional
    public SouscriptionResponse modifierSouscription(Long id, NouvelleSouscriptionRequest request) {
        Souscription souscription = getOuThrow(id);
        // RG1.4 : une PEC ne peut etre modifiee que si son statut est "En cours d'enregistrement".
        if (souscription.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(souscription.getStatut());
        }
        renseignerEtValider(souscription, request);
        souscriptionRepository.save(souscription);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.MODIFICATION,
                "Modification de la souscription " + souscription.getNumeroSouscription());
        return mapper.toResponse(souscription);
    }

    public void supprimerSouscription(Long id) {
        Souscription souscription = getOuThrow(id);
        if (souscription.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(souscription.getStatut());
        }
        souscriptionRepository.delete(souscription);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.SUPPRESSION,
                "Suppression de la souscription " + souscription.getNumeroSouscription());
    }

    /** Renseigne les champs calcules/valides communs a la creation et a la modification. */
    private void renseignerEtValider(Souscription souscription, NouvelleSouscriptionRequest request) {
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(request.getCinRneClient())
                .orElseThrow(ClientNonBnaException::new);

        CompteBnaDto compteDebit = ficheBna.getComptes().stream()
                .filter(c -> c.getNumeroCompte().equals(request.getNumeroCompteBnaDebit()))
                .findFirst()
                .orElseThrow(() -> new TypeCompteNonEligibleException(request.getNumeroCompteBnaDebit()));

        // RG1.3 : seuls les comptes de type 101, 103, 109 et 115 sont eligibles.
        if (!TYPES_COMPTE_ELIGIBLES.contains(compteDebit.getTypeCompte())) {
            throw new TypeCompteNonEligibleException(request.getNumeroCompteBnaDebit());
        }

        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(request.getCinRneClient());
        ProduitBnacDto produitBnac = detailBnac.getProduits().stream()
                .filter(p -> p.getProduit() == request.getProduit()
                        && p.getNumeroCompteTitre().equals(request.getNumeroCompteTitre()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produit " + request.getProduit() + " introuvable pour le compte titre "
                                + request.getNumeroCompteTitre()));

        BigDecimal montant = produitBnac.getValeurLiquidativeSouscription()
                .multiply(BigDecimal.valueOf(request.getNombreActionsASouscrire()));

        // RG1.5 : le montant de souscription ne peut pas depasser la provision du compte BNA choisi.
        if (montant.compareTo(compteDebit.getProvisionDisponible()) > 0) {
            throw new MontantSuperieurProvisionException(montant, compteDebit.getProvisionDisponible());
        }

        souscription.setCinRneClient(request.getCinRneClient());
        souscription.setNumeroCompteTitre(request.getNumeroCompteTitre());
        souscription.setProduit(request.getProduit());
        souscription.setValeurLiquidative(produitBnac.getValeurLiquidativeSouscription());
        souscription.setNombreActionsASouscrire(request.getNombreActionsASouscrire());
        souscription.setMontantSouscription(montant);
        souscription.setNumeroCompteBnaDebit(request.getNumeroCompteBnaDebit());
        souscription.setTypeCompteBnaDebit(compteDebit.getTypeCompte());
        souscription.setActionsEnProcessionAvant(produitBnac.getNombreActionsEnProcession());
        souscription.setDateSouscription(LocalDate.now());
        // RG1.6 : la date valeur comptable est la date de l'operation.
        souscription.setDateValeurComptable(LocalDate.now());
    }

    // ---- Etape 3 : traitement des souscriptions en cours ---------------------------------------

    public List<SouscriptionResponse> listerEnCours(String cinRneClient) {
        return souscriptionRepository
                .findByCinRneClientAndStatut(cinRneClient, StatutOperation.EN_COURS_ENREGISTREMENT)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public SouscriptionResponse getDetail(Long id) {
        return mapper.toResponse(getOuThrow(id));
    }

    public DocumentJoint importerDocument(Long souscriptionId, TypeDocument typeDocument, MultipartFile fichier) {
        getOuThrow(souscriptionId); // 404 si la souscription n'existe pas
        String chemin = documentStorageService.enregistrer(TypeOperation.SOUSCRIPTION, souscriptionId, fichier);

        DocumentJoint document = new DocumentJoint();
        document.setTypeOperation(TypeOperation.SOUSCRIPTION);
        document.setOperationId(souscriptionId);
        document.setTypeDocument(typeDocument);
        document.setNomFichier(fichier.getOriginalFilename());
        document.setCheminStockage(chemin);
        document.setDateImport(java.time.Instant.now());
        document.setImportePar(auditService.operateurCourant());
        DocumentJoint enregistre = documentJointRepository.save(document);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, souscriptionId, TypeAction.IMPORT_DOCUMENT,
                "Import du document " + typeDocument + " (" + fichier.getOriginalFilename() + ")");
        return enregistre;
    }

    // ---- Tache 2 : validation ------------------------------------------------------------------

    @Transactional
    public SouscriptionResponse valider(Long id) {
        Souscription souscription = getOuThrow(id);
        if (souscription.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(souscription.getStatut());
        }

        List<DocumentJoint> documents = documentJointRepository
                .findByTypeOperationAndOperationId(TypeOperation.SOUSCRIPTION, id);
        boolean aOrdreVirement = documents.stream()
                .anyMatch(d -> d.getTypeDocument() == TypeDocument.ORDRE_VIREMENT);
        boolean aBulletinSigne = documents.stream()
                .anyMatch(d -> d.getTypeDocument() == TypeDocument.BULLETIN_SOUSCRIPTION_SIGNE);

        // Etape 4 : import obligatoire = ordre de virement + bulletin de souscription signes.
        if (!aOrdreVirement || !aBulletinSigne) {
            throw new DocumentsManquantsException(
                    "ordre de virement et bulletin de souscription signes (client + chef d'agence)");
        }

        SouscriptionWsRequest wsRequest = SouscriptionWsRequest.builder()
                .numeroSouscription(souscription.getNumeroSouscription())
                .numeroCompteTitre(souscription.getNumeroCompteTitre())
                .produit(souscription.getProduit())
                .nombreActionsASouscrire(souscription.getNombreActionsASouscrire())
                .valeurLiquidative(souscription.getValeurLiquidative())
                .montantSouscription(souscription.getMontantSouscription())
                .dateValeurComptable(souscription.getDateValeurComptable())
                .build();

        SouscriptionWsResponse wsResponse = bnacClient.souscrire(wsRequest);
        if (!wsResponse.isSucces()) {
            auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.APPEL_WS,
                    "WS2 (souscription) - echec : " + wsResponse.getMessageErreur());
            throw new BnacWebServiceException("WS2",
                    "BNA Capitaux a refuse la souscription : " + wsResponse.getMessageErreur(), null);
        }
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.APPEL_WS,
                "WS2 (souscription) - succes, reference " + wsResponse.getReferenceSouscription());

        souscription.setStatut(StatutOperation.VALIDE);
        souscription.setReferenceWs2(wsResponse.getReferenceSouscription());
        souscription.setNouveauNombreActions(wsResponse.getNouveauNombreActions());
        souscription.setWs2Succes(true);
        souscriptionRepository.save(souscription);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.VALIDATION,
                "Souscription " + souscription.getNumeroSouscription() + " validee");
        // RG1.3 (section 1.3, resultat "Valider") : ecriture comptable debit compte client / credit compte produit.
        ecritureComptableService.enregistrer(TypeOperation.SOUSCRIPTION, id,
                souscription.getNumeroCompteBnaDebit(), souscription.getProduit().name(),
                souscription.getMontantSouscription());
        return mapper.toResponse(souscription);
    }

    public SouscriptionResponse rejeter(Long id) {
        Souscription souscription = getOuThrow(id);
        souscription.setStatut(StatutOperation.REJETE);
        souscriptionRepository.save(souscription);
        auditService.enregistrer(TypeOperation.SOUSCRIPTION, id, TypeAction.REJET,
                "Souscription " + souscription.getNumeroSouscription() + " rejetee");
        return mapper.toResponse(souscription);
    }

    // ---- Utilitaires ----------------------------------------------------------------------------

    private Souscription getOuThrow(Long id) {
        return souscriptionRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
    }

    private String genererNumeroSouscription() {
        return "SOUS-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
