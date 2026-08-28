package tn.bna.bnac.assistant;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * L'assistant appelle un service externe (Claude) : ces tests verifient tout ce qui ne depend
 * pas d'une cle API reelle (accessible a tout role authentifie, validation, 401 sans token,
 * message d'erreur explicite quand ANTHROPIC_API_KEY est absente).
 */
class AssistantTest extends IntegrationTestBase {

    @Test
    void chat_sansToken_retourne401() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "messages", List.of(Map.of("role", "user", "content", "Bonjour"))))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chat_sansMessages_retourne400() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("messages", List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_accessibleAuChargeDeDossierEtAuValidateur_maisSansCleApiRetourne503() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of("role", "user", "content", "Pourquoi je ne peux pas valider ce rachat ?"))));

        mockMvc.perform(post("/api/assistant/chat")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ANTHROPIC_API_KEY")));

        mockMvc.perform(post("/api/assistant/chat")
                        .header("Authorization", bearer(tokenValidateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isServiceUnavailable());
    }
}
