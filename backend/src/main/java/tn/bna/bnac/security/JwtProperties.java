package tn.bna.bnac.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Lie la section {@code security.jwt.*} de application.yml. */
@Component
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Cle de signature HMAC. Doit etre remplacee en production (voir application.yml). */
    private String secret = "change_this_secret_in_production";

    /** Duree de validite du token, en millisecondes. */
    private long expirationMs = 3_600_000;
}
