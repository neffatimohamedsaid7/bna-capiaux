package tn.bna.bnac.utilisateur;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.dto.UtilisateurAdminResponse;
import tn.bna.bnac.dto.UtilisateurCreateRequest;
import tn.bna.bnac.dto.UtilisateurStatutRequest;

import java.util.List;

/**
 * Gestion des comptes utilisateurs (section 4 : responsabilite du role ADMIN, "acces complet").
 * Reserve au role ADMIN : creation de compte, activation/desactivation, consultation de la liste.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs (ADMIN)")
public class UtilisateurController {

    private final UtilisateurAdminService utilisateurAdminService;

    @Operation(summary = "Lister les comptes utilisateurs du back-office")
    @GetMapping
    public List<UtilisateurAdminResponse> lister() {
        return utilisateurAdminService.lister();
    }

    @Operation(summary = "Creer un compte utilisateur (charge de dossier, validateur ou admin)")
    @PostMapping
    public ResponseEntity<UtilisateurAdminResponse> creer(@Valid @RequestBody UtilisateurCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurAdminService.creer(request));
    }

    @Operation(summary = "Activer ou desactiver un compte utilisateur")
    @PatchMapping("/{id}/statut")
    public UtilisateurAdminResponse changerStatut(@PathVariable Long id,
                                                   @Valid @RequestBody UtilisateurStatutRequest request) {
        return utilisateurAdminService.changerStatut(id, request.getActif());
    }
}
