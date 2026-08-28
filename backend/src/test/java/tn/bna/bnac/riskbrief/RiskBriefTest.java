package tn.bna.bnac.riskbrief;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le resume de risque appelle un service externe (Claude) : ces tests verifient tout ce qui ne
 * depend pas d'une cle API reelle (restriction de role, operation introuvable, message d'erreur
 * explicite quand ANTHROPIC_API_KEY est absente - le cas par defaut de l'environnement de test).
 */
class RiskBriefTest extends IntegrationTestBase {

    @Test
    void genererResume_estReserveAuxValidateurs() throws Exception {
        long id = idDe(creerSouscriptionValide());

        mockMvc.perform(get("/api/risk-brief/SOUSCRIPTION/" + id).header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isForbidden());
    }

    @Test
    void genererResume_operationIntrouvable_retourne404AvantAppelIA() throws Exception {
        mockMvc.perform(get("/api/risk-brief/SOUSCRIPTION/999999").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isNotFound());
    }

    @Test
    void genererResume_sansCleApiConfiguree_retourne503Explicite() throws Exception {
        long id = idDe(creerSouscriptionValide());

        mockMvc.perform(get("/api/risk-brief/SOUSCRIPTION/" + id).header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ANTHROPIC_API_KEY")));
    }

    private MvcResult creerSouscriptionValide() throws Exception {
        return mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 5,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long idDe(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
