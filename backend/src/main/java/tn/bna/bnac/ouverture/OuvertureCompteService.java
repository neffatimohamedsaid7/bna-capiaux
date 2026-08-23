package tn.bna.bnac.ouverture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.audit.AuditService;
import tn.bna.bnac.common.exception.ClientNonBnaException;
import tn.bna.bnac.common.exception.DocumentsManquantsException;
import tn.bna.bnac.common.exception.OperationIntrouvableException;
import tn.bna.bnac.common.exception.StatutOperationInvalideException;
import tn.bna.bnac.common.storage.DocumentStorageService;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.OuvertureCompte;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.domain.TypeAction;
import tn.bna.bnac.domain.TypeDocument;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.NouvelleOuvertureRequest;
import tn.bna.bnac.dto.OuvertureCompteResponse;
import tn.bna.bnac.dto.RechercheClientOuvertureResponse;
import tn.bna.bnac.referentiel.ClientBnaService;
import tn.bna.bnac.dto.ClientBnaDto;
import tn.bna.bnac.repository.DocumentJointRepository;
import tn.bna.bnac.repository.OuvertureCompteRepository;
import tn.bna.bnac.ws.BnacClient;
import tn.bna.bnac.ws.BnacWebServiceException;
import tn.bna.bnac.dto.ClientBnacDetailResponse;
import tn.bna.bnac.dto.OuvertureCompteWsRequest;
import tn.bna.bnac.dto.OuvertureCompteWsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Module 3 - Ouverture de compte titre : etapes 1 a 4 (recherche client, nouvelle demande,
 * traitement des demandes en cours, validation) et regles de gestion RG3.1 a RG3.3.
 */
@Service
@RequiredArgsConstructor
public class OuvertureCompteService {

    private final ClientBnaService clientBnaService;
    private final BnacClient bnacClient;
    private final OuvertureCompteRepository ouvertureCompteRepository;
    private final DocumentJointRepository documentJointRepository;
    private final DocumentStorageService documentStorageService;
    private final OuvertureCompteMapper mapper;
    private final AuditService auditService;

    // ---- Etape 1 : recherche et verification client ----------------------------------------

    public RechercheClientOuvertureResponse rechercherClient(String critereRecherche) {
        // RG3.1 : seuls les clients BNA sont eligibles a l'ouverture d'un compte titre.
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(critereRecherche)
                .orElseThrow(ClientNonBnaException::new);

        ClientBnacDetailResponse detailBnac = bnacClient.detailClient(critereRecherche);

        return RechercheClientOuvertureResponse.builder()
                .ficheBna(ficheBna)
                .possedeCompteTitre(detailBnac.isPossedeCompteTitre())
                .produitsExistants(detailBnac.isPossedeCompteTitre() ? detailBnac.getProduits() : List.of())
                .build();
    }

    // ---- Etape 2 : nouvelle demande d'ouverture -----------------------------------------------

    @Transactional
    public OuvertureCompteResponse creerDemande(NouvelleOuvertureRequest request) {
        OuvertureCompte demande = new OuvertureCompte();
        demande.setNumeroDemande(genererNumeroDemande());
        renseigner(demande, request);
        demande.setStatut(StatutOperation.EN_COURS_ENREGISTREMENT);
        ouvertureCompteRepository.save(demande);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, demande.getId(), TypeAction.CREATION,
                "PEC ouverture de compte " + demande.getNumeroDemande());
        return mapper.toResponse(demande);
    }

    @Transactional
    public OuvertureCompteResponse modifierDemande(Long id, NouvelleOuvertureRequest request) {
        OuvertureCompte demande = getOuThrow(id);
        // RG3.2 : la PEC ne peut etre modifiee que si son statut est "En cours d'enregistrement".
        if (demande.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(demande.getStatut());
        }
        renseigner(demande, request);
        ouvertureCompteRepository.save(demande);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.MODIFICATION,
                "Modification de la demande " + demande.getNumeroDemande());
        return mapper.toResponse(demande);
    }

    public void supprimerDemande(Long id) {
        OuvertureCompte demande = getOuThrow(id);
        if (demande.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(demande.getStatut());
        }
        ouvertureCompteRepository.delete(demande);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.SUPPRESSION,
                "Suppression de la demande " + demande.getNumeroDemande());
    }

    private void renseigner(OuvertureCompte demande, NouvelleOuvertureRequest request) {
        ClientBnaDto ficheBna = clientBnaService.rechercherClient(request.getCinRneClient())
                .orElseThrow(ClientNonBnaException::new);

        demande.setCinRneClient(request.getCinRneClient());
        demande.setNomPrenomClient(ficheBna.getNomPrenom());
        demande.setAdresseClient(ficheBna.getAdresse());
        demande.setActiviteClient(ficheBna.getActivite());
        demande.setTypeCompteSouhaite(request.getTypeCompteSouhaite());
        demande.setDateDemande(LocalDate.now());
    }

    // ---- Etape 3 : traitement des demandes en cours --------------------------------------------

    public List<OuvertureCompteResponse> listerEnCours(String cinRneClient) {
        return ouvertureCompteRepository
                .findByCinRneClientAndStatut(cinRneClient, StatutOperation.EN_COURS_ENREGISTREMENT)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public OuvertureCompteResponse getDetail(Long id) {
        return mapper.toResponse(getOuThrow(id));
    }

    public DocumentJoint importerDocument(Long demandeId, TypeDocument typeDocument, MultipartFile fichier) {
        getOuThrow(demandeId);
        String chemin = documentStorageService.enregistrer(TypeOperation.OUVERTURE_COMPTE, demandeId, fichier);

        DocumentJoint document = new DocumentJoint();
        document.setTypeOperation(TypeOperation.OUVERTURE_COMPTE);
        document.setOperationId(demandeId);
        document.setTypeDocument(typeDocument);
        document.setNomFichier(fichier.getOriginalFilename());
        document.setCheminStockage(chemin);
        document.setDateImport(java.time.Instant.now());
        document.setImportePar(auditService.operateurCourant());
        DocumentJoint enregistre = documentJointRepository.save(document);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, demandeId, TypeAction.IMPORT_DOCUMENT,
                "Import du document " + typeDocument + " (" + fichier.getOriginalFilename() + ")");
        return enregistre;
    }

    // ---- Tache 2 : validation --------------------------------------------------------------------

    @Transactional
    public OuvertureCompteResponse valider(Long id) {
        OuvertureCompte demande = getOuThrow(id);
        if (demande.getStatut() != StatutOperation.EN_COURS_ENREGISTREMENT) {
            throw new StatutOperationInvalideException(demande.getStatut());
        }

        List<DocumentJoint> documents = documentJointRepository
                .findByTypeOperationAndOperationId(TypeOperation.OUVERTURE_COMPTE, id);
        boolean aFormulaireBnac = documents.stream()
                .anyMatch(d -> d.getTypeDocument() == TypeDocument.FORMULAIRE_COMPTE_BNAC);
        boolean aCin = documents.stream()
                .anyMatch(d -> d.getTypeDocument() == TypeDocument.CIN);

        // Etape 4 : import obligatoire = formulaire compte BNAC + CIN.
        if (!aFormulaireBnac || !aCin) {
            throw new DocumentsManquantsException("formulaire compte BNAC et CIN");
        }

        // RG3.3 : pour les clients sans compte BNAC, les donnees signaletiques BNA sont
        // transmises directement via WS4 (elles sont deja portees par l'entite OuvertureCompte).
        OuvertureCompteWsRequest wsRequest = OuvertureCompteWsRequest.builder()
                .numeroDemande(demande.getNumeroDemande())
                .identifiantClient(demande.getCinRneClient())
                .typeIdentifiant("CIN")
                .nomPrenom(demande.getNomPrenomClient())
                .adresse(demande.getAdresseClient())
                .activite(demande.getActiviteClient())
                .typeCompteSouhaite(demande.getTypeCompteSouhaite())
                .build();

        OuvertureCompteWsResponse wsResponse = bnacClient.ouvrirCompte(wsRequest);
        if (!wsResponse.isSucces()) {
            auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.APPEL_WS,
                    "WS4 (ouverture compte) - echec : " + wsResponse.getMessageErreur());
            throw new BnacWebServiceException("WS4",
                    "BNA Capitaux a refuse l'ouverture de compte : " + wsResponse.getMessageErreur(), null);
        }
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.APPEL_WS,
                "WS4 (ouverture compte) - succes, reference " + wsResponse.getReferenceOuverture());

        demande.setStatut(StatutOperation.VALIDE);
        demande.setReferenceWs4(wsResponse.getReferenceOuverture());
        demande.setNumeroCompteTitreGenere(wsResponse.getNumeroCompteTitre());
        demande.setWs4Succes(true);
        ouvertureCompteRepository.save(demande);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.VALIDATION,
                "Demande " + demande.getNumeroDemande() + " validee");
        return mapper.toResponse(demande);
    }

    public OuvertureCompteResponse rejeter(Long id) {
        OuvertureCompte demande = getOuThrow(id);
        demande.setStatut(StatutOperation.REJETE);
        ouvertureCompteRepository.save(demande);
        auditService.enregistrer(TypeOperation.OUVERTURE_COMPTE, id, TypeAction.REJET,
                "Demande " + demande.getNumeroDemande() + " rejetee");
        return mapper.toResponse(demande);
    }

    // ---- Utilitaires ------------------------------------------------------------------------------

    private OuvertureCompte getOuThrow(Long id) {
        return ouvertureCompteRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
    }

    private String genererNumeroDemande() {
        return "OUV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
