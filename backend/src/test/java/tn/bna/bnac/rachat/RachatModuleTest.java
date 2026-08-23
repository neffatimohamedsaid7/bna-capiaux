package tn.bna.bnac.rachat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Module 2 - Demande de rachat : RG2.1 a RG2.3 et autorisation par role. */
class RachatModuleTest extends IntegrationTestBase {

    @Test
    void creerRachat_nombreActionsSuperieurAuDisponible_estRejete() throws Exception {
        // RG2.2 : client 12345678 / FCP_PROGRES a 10 actions en procession, 0 rachat en cours,
        // 0 en attente BNAC -> disponible = 10. Demander 11 doit echouer.
        mockMvc.perform(post("/api/rachats")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", 11,
                                "numeroCompteBnaCredit", "01100012345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("depasse le nombre disponible")));
    }

    @Test
    void creerRachat_compteCreditNAppartenantPasAuClient_estRejete() throws Exception {
        mockMvc.perform(post("/api/rachats")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", 1,
                                "numeroCompteBnaCredit", "00000000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerRachat_valide_appliqueRG23DateValeurPlusUnJour() throws Exception {
        MvcResult result = creerRachatValide(5);
        var json = objectMapper.readTree(result.getResponse().getContentAsString());

        LocalDate dateRachat = LocalDate.parse(json.get("dateRachat").asText());
        LocalDate dateValeurComptable = LocalDate.parse(json.get("dateValeurComptable").asText());

        assertThat(dateValeurComptable).isEqualTo(dateRachat.plusDays(1));
        assertThat(json.get("montantRachat").decimalValue())
                .isEqualByComparingTo("624.500"); // 5 x 124.900
    }

    @Test
    void cycleDeVie_modifierUniquementEnCoursDEnregistrement() throws Exception {
        // RG2.1
        long id = idDe(creerRachatValide(2));

        mockMvc.perform(post("/api/rachats/" + id + "/rejeter").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REJETE"));

        mockMvc.perform(put("/api/rachats/" + id)
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", 3,
                                "numeroCompteBnaCredit", "01100012345"))))
                .andExpect(status().isConflict());
    }

    @Test
    void supprimer_unRachatEnCours_reussit() throws Exception {
        long id = idDe(creerRachatValide(1));

        mockMvc.perform(delete("/api/rachats/" + id).header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isNoContent());
    }

    @Test
    void valider_sansBulletinSigne_estBloquee() throws Exception {
        long id = idDe(creerRachatValide(1));

        mockMvc.perform(post("/api/rachats/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void valider_avecBulletinSigne_appelleWS3EtPasseAuStatutValide() throws Exception {
        long id = idDe(creerRachatValide(1));
        importerBulletinSigne(id);

        mockMvc.perform(post("/api/rachats/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"))
                .andExpect(jsonPath("$.referenceWs3").isNotEmpty());
    }

    @Test
    void role_chargeDeDossierNePeutPasValider() throws Exception {
        long id = idDe(creerRachatValide(1));
        importerBulletinSigne(id);

        mockMvc.perform(post("/api/rachats/" + id + "/valider").header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isForbidden());
    }

    @Test
    void role_validateurNePeutPasCreerDePEC() throws Exception {
        mockMvc.perform(post("/api/rachats")
                        .header("Authorization", bearer(tokenValidateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", 1,
                                "numeroCompteBnaCredit", "01100012345"))))
                .andExpect(status().isForbidden());
    }

    // ---- Utilitaires ------------------------------------------------------------------------------

    private MvcResult creerRachatValide(int nombreActions) throws Exception {
        return mockMvc.perform(post("/api/rachats")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", nombreActions,
                                "numeroCompteBnaCredit", "01100012345"))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long idDe(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void importerBulletinSigne(long id) throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "bulletin-rachat.pdf", "application/pdf", "contenu".getBytes());
        mockMvc.perform(multipart("/api/rachats/" + id + "/documents")
                        .file(fichier)
                        .param("typeDocument", "BULLETIN_RACHAT_SIGNE")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());
    }
}
