package tn.bna.bnac.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Swagger / OpenAPI.
 * Documentation accessible sur /swagger-ui.html une fois l'application demarree.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI bnacOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BNA Capitaux - Souscription et Rachat des Actions")
                        .description("API REST pour la PEC et la validation des operations de "
                                + "souscription d'actions, demande de rachat, ouverture de compte "
                                + "titre et consultation, avec interfacage BNAC (WS1 a WS4).")
                        .version("v0.1")
                        .contact(new Contact().name("BNA - Equipe projet BNA Capitaux")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .schemaRequirement(BEARER_SCHEME, new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
