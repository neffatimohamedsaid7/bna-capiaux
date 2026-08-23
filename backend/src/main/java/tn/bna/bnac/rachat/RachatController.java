package tn.bna.bnac.rachat;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.TypeDocument;
import tn.bna.bnac.dto.NouveauRachatRequest;
import tn.bna.bnac.dto.RachatResponse;
import tn.bna.bnac.dto.RechercheClientRachatResponse;

import java.util.List;

/**
 * Module 2 - Demande de rachat.
 * PEC : recherche client -> nouveau rachat -> suivi des rachats en cours.
 * Validation : import du bulletin signe + appel WS3.
 */
@RestController
@RequestMapping("/api/rachats")
@RequiredArgsConstructor
@Tag(name = "Rachats", description = "Module 2 - Demande de rachat")
public class RachatController {

    private final RachatService rachatService;
    private final RachatPdfService rachatPdfService;

    @Operation(summary = "Etape 1 - Recherche et verification client (CIN, RNE, N° compte ou N° titre)")
    @GetMapping("/recherche-client")
    public RechercheClientRachatResponse rechercherClient(@RequestParam String critere) {
        return rachatService.rechercherClient(critere);
    }

    @Operation(summary = "Etape 2 - Enregistrer un nouveau rachat (PEC)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RachatResponse> creer(@Valid @RequestBody NouveauRachatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rachatService.creerRachat(request));
    }

    @Operation(summary = "Modifier un rachat en cours d'enregistrement (RG2.1)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @PutMapping("/{id}")
    public RachatResponse modifier(@PathVariable Long id, @Valid @RequestBody NouveauRachatRequest request) {
        return rachatService.modifierRachat(id, request);
    }

    @Operation(summary = "Supprimer un rachat en cours d'enregistrement (RG2.1)")
    @PreAuthorize("hasAnyRole('CHARGE_DE_DOSSIER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        rachatService.supprimerRachat(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Etape 3 - Rachats en cours d'enregistrement pour un client")
    @GetMapping("/en-cours")
    public List<RachatResponse> listerEnCours(@RequestParam String cinRneClient) {
        return rachatService.listerEnCours(cinRneClient);
    }

    @Operation(summary = "Detail d'un rachat")
    @GetMapping("/{id}")
    public RachatResponse getDetail(@PathVariable Long id) {
        return rachatService.getDetail(id);
    }

    @Operation(summary = "Importer une piece jointe (bulletin de rachat signe, ...)")
    @PostMapping(value = "/{id}/documents", consumes = "multipart/form-data")
    public DocumentJoint importerDocument(@PathVariable Long id,
                                           @RequestParam TypeDocument typeDocument,
                                           @RequestPart MultipartFile fichier) {
        return rachatService.importerDocument(id, typeDocument, fichier);
    }

    @Operation(summary = "Etape 4 - Valider la demande de rachat (appel WS3)")
    @PreAuthorize("hasAnyRole('VALIDATEUR', 'ADMIN')")
    @PostMapping("/{id}/valider")
    public RachatResponse valider(@PathVariable Long id) {
        return rachatService.valider(id);
    }

    @Operation(summary = "Etape 4 - Rejeter la demande de rachat")
    @PreAuthorize("hasAnyRole('VALIDATEUR', 'ADMIN')")
    @PostMapping("/{id}/rejeter")
    public RachatResponse rejeter(@PathVariable Long id) {
        return rachatService.rejeter(id);
    }

    @Operation(summary = "Imprimer la liste des produits detenus par le client, pour le rachat (Annexe 3)")
    @GetMapping(value = "/recherche-client/impression", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> impressionListeProduits(@RequestParam String critere) {
        RechercheClientRachatResponse resultat = rachatService.rechercherClient(critere);
        byte[] pdf = rachatPdfService.genererListeProduits(resultat);
        return pdfResponse(pdf, "liste-produits-rachat-" + critere + ".pdf");
    }

    @Operation(summary = "Imprimer le bulletin de rachat (Annexe 4)")
    @GetMapping(value = "/{id}/bulletin", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> bulletin(@PathVariable Long id) {
        RachatResponse r = rachatService.getDetail(id);
        byte[] pdf = rachatPdfService.genererBulletin(r);
        return pdfResponse(pdf, "bulletin-rachat-" + r.getNumeroRachat() + ".pdf");
    }

    @Operation(summary = "Imprimer la decharge (disponible apres validation, appel WS3)")
    @GetMapping(value = "/{id}/decharge", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> decharge(@PathVariable Long id) {
        RachatResponse r = rachatService.getDetail(id);
        byte[] pdf = rachatPdfService.genererDecharge(r);
        return pdfResponse(pdf, "decharge-" + r.getNumeroRachat() + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String nomFichier) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
