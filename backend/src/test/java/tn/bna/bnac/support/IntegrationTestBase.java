package tn.bna.bnac.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Socle commun des tests d'integration : demarre le contexte complet (H2, stubs BNA/BNAC,
 * securite JWT reelle) et authentifie les 3 comptes de demonstration crees par
 * {@code UtilisateurSeeder} pour tester les regles d'autorisation par role.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String tokenAdmin;
    protected String tokenAgent;
    protected String tokenValidateur;

    @BeforeEach
    void authentifierComptesDemo() throws Exception {
        tokenAdmin = login("admin", "Admin123!");
        tokenAgent = login("agent1", "Agent123!");
        tokenValidateur = login("validateur1", "Valid123!");
    }

    protected String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
