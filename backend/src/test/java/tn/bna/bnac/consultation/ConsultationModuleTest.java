package tn.bna.bnac.consultation;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Module 4 - Consultation et edition : RG4.1/RG4.2 et filtres (produit, etat, date). */
class ConsultationModuleTest extends IntegrationTestBase {

    @Test
    void portefeuille_clientSansCompteTitre_estBloque() throws Exception {
        // RG4.2 : la consultation necessite un compte titre BNAC actif.
        mockMvc.perform(get("/api/consultation/portefeuille")
                        .param("critere", "11112222")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le client ne dispose pas un compte chez BNA capitaux"));
    }

    @Test
    void portefeuille_clientAvecCompteTitre_retourneLesProduits() throws Exception {
        // RG4.1 : consultable depuis n'importe quelle agence (aucune restriction d'agence en base).
        mockMvc.perform(get("/api/consultation/portefeuille")
                        .param("critere", "12345678")
                        .header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produits.length()").value(2));
    }

    @Test
    void historiqueSouscriptions_filtreParEtat_neRenvoiQueLesSouscriptionsCorrespondantes() throws Exception {
        long idSouscriptionEnCours = idDe(creerSouscription("12345678", 1));
        long idSouscriptionRejetee = idDe(creerSouscription("12345678", 1));
        mockMvc.perform(post("/api/souscriptions/" + idSouscriptionRejetee + "/rejeter")
                        .header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/consultation/souscriptions")
                        .param("critere", "12345678")
                        .param("etat", "REJETE")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idSouscription != null)]").exists());

        // La liste filtree "EN_COURS_ENREGISTREMENT" ne doit pas contenir l'operation rejetee.
        MvcResult resultEnCours = mockMvc.perform(get("/api/consultation/souscriptions")
                        .param("critere", "12345678")
                        .param("etat", "EN_COURS_ENREGISTREMENT")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andReturn();
        var souscriptionsEnCours = objectMapper.readTree(resultEnCours.getResponse().getContentAsString());
        boolean contientLaRejetee = false;
        for (var node : souscriptionsEnCours) {
            if (node.get("idSouscription").asText().equals(numeroDe(idSouscriptionRejetee))) {
                contientLaRejetee = true;
            }
        }
        org.assertj.core.api.Assertions.assertThat(contientLaRejetee).isFalse();
        org.assertj.core.api.Assertions.assertThat(idSouscriptionEnCours).isNotZero();
    }

    @Test
    void historiqueSouscriptions_filtreParProduitAbsent_neRenvoieRien() throws Exception {
        creerSouscription("12345678", 1);

        mockMvc.perform(get("/api/consultation/souscriptions")
                        .param("critere", "12345678")
                        .param("produit", "SICAV") // aucune souscription creee pour ce produit dans ce test
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- Utilitaires ------------------------------------------------------------------------------

    private MvcResult creerSouscription(String cinRneClient, int nombreActions) throws Exception {
        return mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", cinRneClient,
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", nombreActions,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long idDe(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String numeroDe(long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/souscriptions/" + id).header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("numeroSouscription").asText();
    }
}
