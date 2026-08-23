package tn.bna.bnac.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.bna.bnac.domain.Role;
import tn.bna.bnac.domain.Utilisateur;
import tn.bna.bnac.repository.UtilisateurRepository;

/**
 * Jeu de comptes de demonstration (equivalent, pour l'authentification, de ClientBnaServiceStub) :
 * cree au premier demarrage si la table utilisateur est vide, pour permettre de se connecter
 * sans etape d'administration prealable. A retirer/adapter avant mise en production.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UtilisateurSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }

        creer("admin", "Admin123!", "Neffati", "Medsaid", Role.ADMIN);
        creer("agent1", "Agent123!", "Karray", "Mehdi", Role.CHARGE_DE_DOSSIER);
        creer("validateur1", "Valid123!", "Jlassi", "Wafa", Role.VALIDATEUR);

        log.info("Comptes de demonstration crees : admin/Admin123!, agent1/Agent123!, validateur1/Valid123!");
    }

    private void creer(String username, String rawPassword, String nom, String prenom, Role role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(username);
        utilisateur.setPassword(passwordEncoder.encode(rawPassword));
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }
}
