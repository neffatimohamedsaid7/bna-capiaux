package tn.bna.bnac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de securite temporaire : ouvre l'API pendant le developpement des modules
 * fonctionnels (1 a 4) afin de pouvoir tester via Swagger sans authentification.
 * <p>
 * TODO (avant mise en production / recette) : remplacer par l'authentification JWT decrite
 * en section 6.1 du cahier des charges (filtre de validation de token, gestion des roles
 * "Charge de dossier" / "Validateur"), et restreindre les endpoints en consequence.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
