package tn.bna.bnac.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.bna.bnac.dto.LoginRequest;
import tn.bna.bnac.dto.LoginResponse;
import tn.bna.bnac.dto.UtilisateurResponse;
import tn.bna.bnac.domain.Utilisateur;
import tn.bna.bnac.repository.UtilisateurRepository;
import tn.bna.bnac.security.JwtService;

/** Connexion au back-office : verifie les identifiants et emet un token JWT. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // Delegue a Spring Security (UtilisateurDetailsService + BCrypt) ; leve
        // BadCredentialsException si le mot de passe est incorrect ou l'utilisateur inconnu/desactive.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Utilisateur utilisateur = utilisateurRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + request.getUsername()));

        String token = jwtService.generateToken(
                utilisateur.getUsername(), utilisateur.getRole(), utilisateur.getNom(), utilisateur.getPrenom());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .username(utilisateur.getUsername())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .build();
    }

    public UtilisateurResponse me(String username) {
        Utilisateur utilisateur = utilisateurRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + username));

        return UtilisateurResponse.builder()
                .username(utilisateur.getUsername())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .build();
    }
}
