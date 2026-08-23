package tn.bna.bnac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Compte utilisateur du back-office (agence). Authentification par mot de passe (hash BCrypt),
 * emission d'un token JWT a la connexion (voir package {@code tn.bna.bnac.security}).
 */
@Entity
@Table(name = "utilisateur")
@Getter
@Setter
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 64)
    private String username;

    /** Mot de passe hache (BCrypt) - jamais expose dans les DTO de reponse. */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nom", nullable = false, length = 64)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 64)
    private String prenom;

    @Column(name = "email", length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 24)
    private Role role;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
