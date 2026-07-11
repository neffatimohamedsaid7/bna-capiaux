package tn.bna.bnac.ouverture;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import tn.bna.bnac.ouverture.dto.NouvelleOuvertureRequest;
import tn.bna.bnac.ouverture.dto.OuvertureCompteResponse;
import tn.bna.bnac.ouverture.dto.RechercheClientOuvertureResponse;

import java.util.List;

/**
 * Module 3 - Ouverture de compte titre.
 * PEC : recherche client -> nouvelle demande -> suivi des demandes en cours.
 * Validation : import des pieces + appel WS4 (RG3.3 pour les clients sans compte BNAC).
 */
@RestController
@RequestMapping("/api/ouvertures-compte")
@RequiredArgsConstructor
@Tag(name = "Ouvertures de compte", description = "Module 3 - Ouverture de compte titre")
public class OuvertureCompteController {

    private final OuvertureCompteService ouvertureCompteService;

    @Operation(summary = "Etape 1 - Recherche et verification client (CIN, RNE, N° compte ou N° titre)")
    @GetMapping("/recherche-client")
    public RechercheClientOuvertureResponse rechercherClient(@RequestParam String critere) {
        return ouvertureCompteService.rechercherClient(critere);
    }

    @Operation(summary = "Etape 2 - Enregistrer une nouvelle demande d'ouverture (PEC)")
    @PostMapping
    public ResponseEntity<OuvertureCompteResponse> creer(@Valid @RequestBody NouvelleOuvertureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ouvertureCompteService.creerDemande(request));
    }

    @Operation(summary = "Modifier une demande en cours d'enregistrement (RG3.2)")
    @PutMapping("/{id}")
    public OuvertureCompteResponse modifier(@PathVariable Long id,
                                             @Valid @RequestBody NouvelleOuvertureRequest request) {
        return ouvertureCompteService.modifierDemande(id, request);
    }

    @Operation(summary = "Supprimer une demande en cours d'enregistrement (RG3.2)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        ouvertureCompteService.supprimerDemande(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Etape 3 - Demandes en cours d'enregistrement pour un client")
    @GetMapping("/en-cours")
    public List<OuvertureCompteResponse> listerEnCours(@RequestParam String cinRneClient) {
        return ouvertureCompteService.listerEnCours(cinRneClient);
    }

    @Operation(summary = "Detail d'une demande d'ouverture")
    @GetMapping("/{id}")
    public OuvertureCompteResponse getDetail(@PathVariable Long id) {
        return ouvertureCompteService.getDetail(id);
    }

    @Operation(summary = "Importer une piece jointe (formulaire compte BNAC, CIN, ...)")
    @PostMapping(value = "/{id}/documents", consumes = "multipart/form-data")
    public DocumentJoint importerDocument(@PathVariable Long id,
                                           @RequestParam TypeDocument typeDocument,
                                           @RequestPart MultipartFile fichier) {
        return ouvertureCompteService.importerDocument(id, typeDocument, fichier);
    }

    @Operation(summary = "Etape 4 - Valider la demande d'ouverture (appel WS4)")
    @PostMapping("/{id}/valider")
    public OuvertureCompteResponse valider(@PathVariable Long id) {
        return ouvertureCompteService.valider(id);
    }

    @Operation(summary = "Etape 4 - Rejeter la demande d'ouverture")
    @PostMapping("/{id}/rejeter")
    public OuvertureCompteResponse rejeter(@PathVariable Long id) {
        return ouvertureCompteService.rejeter(id);
    }
}
