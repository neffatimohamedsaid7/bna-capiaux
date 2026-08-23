package tn.bna.bnac.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.dto.LoginRequest;
import tn.bna.bnac.dto.LoginResponse;
import tn.bna.bnac.dto.UtilisateurResponse;

/** Authentification du back-office (section 6.1 du cahier des charges). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Connexion et session utilisateur")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Connexion : verifie les identifiants et retourne un token JWT")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Utilisateur courant, a partir du token envoye")
    @GetMapping("/me")
    public UtilisateurResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}
