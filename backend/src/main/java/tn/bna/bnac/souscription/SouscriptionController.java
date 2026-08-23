package tn.bna.bnac.souscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.TypeDocument;
import tn.bna.bnac.dto.NouvelleSouscriptionRequest;
import tn.bna.bnac.dto.RechercheClientSouscriptionResponse;
import tn.bna.bnac.dto.SouscriptionResponse;

import java.util.List;

/**
 * Module 1 - Souscription d'actions.
 * PEC : recherche client -> nouvelle souscription -> suivi des souscriptions en cours.
 * Validation : import des pieces + appel WS2.
 */
@RestController
@RequestMapping("/api/souscriptions")
@RequiredArgsConstructor
@Tag(name = "Souscriptions", description = "Module 1 - Souscription d'actions")
public class SouscriptionController {

    private final SouscriptionService souscriptionService;
    private final SouscriptionPdfService souscriptionPdfService;

    @Operation(summary = "Etape 1 - Recherche et verification client (CIN, RNE, N° compte ou N° titre)")
    @GetMapping("/recherche-client")
    public RechercheClientSouscriptionResponse rechercherClient(@RequestParam String critere) {
        return souscriptionService.rechercherClient(critere);
    }

    @Operation(summary = "Etape 2 - Enregistrer une nouvelle souscription (PEC)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SouscriptionResponse> creer(@Valid @RequestBody NouvelleSouscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(souscriptionService.creerSouscription(request));
    }

    @Operation(summary = "Modifier une souscription en cours d'enregistrement (RG1.4)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @PutMapping("/{id}")
    public SouscriptionResponse modifier(@PathVariable Long id,
                                          @Valid @RequestBody NouvelleSouscriptionRequest request) {
        return souscriptionService.modifierSouscription(id, request);
    }

    @Operation(summary = "Supprimer une souscription en cours d'enregistrement (RG1.4)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        souscriptionService.supprimerSouscription(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Etape 3 - Souscriptions en cours d'enregistrement pour un client")
    @GetMapping("/en-cours")
    public List<SouscriptionResponse> listerEnCours(@RequestParam String cinRneClient) {
        return souscriptionService.listerEnCours(cinRneClient);
    }

    @Operation(summary = "Detail d'une souscription")
    @GetMapping("/{id}")
    public SouscriptionResponse getDetail(@PathVariable Long id) {
        return souscriptionService.getDetail(id);
    }

    @Operation(summary = "Importer une piece jointe (ordre de virement, bulletin signe, ...)")
    @PostMapping(value = "/{id}/documents", consumes = "multipart/form-data")
    public DocumentJoint importerDocument(@PathVariable Long id,
                                           @RequestParam TypeDocument typeDocument,
                                           @RequestPart MultipartFile fichier) {
        return souscriptionService.importerDocument(id, typeDocument, fichier);
    }

    @Operation(summary = "Etape 4 - Valider la souscription (appel WS2)")
    @PreAuthorize("hasAnyRole('VALIDATEUR', 'ADMIN')")
    @PostMapping("/{id}/valider")
    public SouscriptionResponse valider(@PathVariable Long id) {
        return souscriptionService.valider(id);
    }

    @Operation(summary = "Etape 4 - Rejeter la souscription")
    @PreAuthorize("hasAnyRole('VALIDATEUR', 'ADMIN')")
    @PostMapping("/{id}/rejeter")
    public SouscriptionResponse rejeter(@PathVariable Long id) {
        return souscriptionService.rejeter(id);
    }

    @Operation(summary = "Imprimer la liste des produits detenus par le client (Annexe 1)")
    @GetMapping(value = "/recherche-client/impression", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> impressionListeProduits(@RequestParam String critere) {
        RechercheClientSouscriptionResponse resultat = souscriptionService.rechercherClient(critere);
        byte[] pdf = souscriptionPdfService.genererListeProduits(resultat);
        return pdfResponse(pdf, "liste-produits-souscription-" + critere + ".pdf");
    }

    @Operation(summary = "Imprimer le bulletin de souscription (Annexe 2)")
    @GetMapping(value = "/{id}/bulletin", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> bulletin(@PathVariable Long id) {
        SouscriptionResponse s = souscriptionService.getDetail(id);
        byte[] pdf = souscriptionPdfService.genererBulletin(s);
        return pdfResponse(pdf, "bulletin-souscription-" + s.getNumeroSouscription() + ".pdf");
    }

    @Operation(summary = "Imprimer l'avis d'operation (disponible apres validation, appel WS2)")
    @GetMapping(value = "/{id}/avis-operation", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> avisOperation(@PathVariable Long id) {
        SouscriptionResponse s = souscriptionService.getDetail(id);
        byte[] pdf = souscriptionPdfService.genererAvisOperation(s);
        return pdfResponse(pdf, "avis-operation-" + s.getNumeroSouscription() + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String nomFichier) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
