package tn.bna.bnac.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.bna.bnac.support.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Authentification JWT (section 6.1 du cahier des charges) : login, /me, protection des routes. */
class AuthFlowTest extends IntegrationTestBase {

    @Test
    void loginAvecIdentifiantsValides_retourneUnTokenEtLeRoleAttendu() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginAvecMotDePasseIncorrect_retourne401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mauvais-mdp\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides"));
    }

    @Test
    void loginAvecUtilisateurInconnu_retourne401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inconnu\",\"password\":\"peu-importe\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meAvecTokenValide_retourneLeProfilDeLUtilisateurConnecte() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("validateur1"))
                .andExpect(jsonPath("$.role").value("VALIDATEUR"));
    }

    @Test
    void routeProtegeeSansToken_retourne401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void routeProtegeeAvecTokenInvalide_retourne401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", "Bearer token-invalide"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void routeProtegeeAvecTokenValide_retourne200() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
    }
}
