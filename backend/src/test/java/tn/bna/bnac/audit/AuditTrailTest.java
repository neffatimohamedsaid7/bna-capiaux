package tn.bna.bnac.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.domain.DocumentJoint;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.repository.DocumentJointRepository;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Journal d'audit (section 6.4 du cahier des charges) : trace chaque action avec operateur et horodatage. */
class AuditTrailTest extends IntegrationTestBase {

    @Autowired
    private DocumentJointRepository documentJointRepository;

    @Test
    void cycleDeVieComplet_tracesLesActionsDansLOrdreEtAvecLOperateurCorrect() throws Exception {
        // Creation par l'agent.
        MvcResult creation = mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 2,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();

        // Modification par l'agent.
        mockMvc.perform(put("/api/souscriptions/" + id)
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 3,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isOk());

        // Import des pieces par l'agent.
        MockMultipartFile ordreVirement = new MockMultipartFile("fichier", "ordre.pdf", "application/pdf", "contenu".getBytes());
        MockMultipartFile bulletinSigne = new MockMultipartFile("fichier", "bulletin.pdf", "application/pdf", "contenu".getBytes());
        mockMvc.perform(multipart("/api/souscriptions/" + id + "/documents")
                        .file(ordreVirement).param("typeDocument", "ORDRE_VIREMENT")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());
        mockMvc.perform(multipart("/api/souscriptions/" + id + "/documents")
                        .file(bulletinSigne).param("typeDocument", "BULLETIN_SOUSCRIPTION_SIGNE")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        // Validation par le validateur.
        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk());

        // Historique complet, du plus recent au plus ancien.
        MvcResult historique = mockMvc.perform(get("/api/audit/SOUSCRIPTION/" + id).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andReturn();
        var entrees = objectMapper.readTree(historique.getResponse().getContentAsString());

        assertThat(entrees).hasSize(6);
        List<String> typesActionDansLOrdre = List.of(
                entrees.get(0).get("typeAction").asText(),
                entrees.get(1).get("typeAction").asText(),
                entrees.get(2).get("typeAction").asText(),
                entrees.get(3).get("typeAction").asText(),
                entrees.get(4).get("typeAction").asText(),
                entrees.get(5).get("typeAction").asText());
        assertThat(typesActionDansLOrdre).containsExactly(
                "VALIDATION", "APPEL_WS", "IMPORT_DOCUMENT", "IMPORT_DOCUMENT", "MODIFICATION", "CREATION");

        // L'operateur de la validation est le validateur, celui de la creation est l'agent.
        assertThat(entrees.get(0).get("operateur").asText()).isEqualTo("validateur1");
        assertThat(entrees.get(5).get("operateur").asText()).isEqualTo("agent1");
    }

    @Test
    void rejeter_estTraceAvecLOperateurQuiARejete() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/rachats")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsAVendre", 1,
                                "numeroCompteBnaCredit", "01100012345"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/rachats/" + id + "/rejeter").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk());

        MvcResult historique = mockMvc.perform(get("/api/audit/RACHAT/" + id).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andReturn();
        var entrees = objectMapper.readTree(historique.getResponse().getContentAsString());

        assertThat(entrees).hasSize(2);
        assertThat(entrees.get(0).get("typeAction").asText()).isEqualTo("REJET");
        assertThat(entrees.get(0).get("operateur").asText()).isEqualTo("validateur1");
    }

    @Test
    void importerDocument_renseigneLOperateurSurLaPieceJointe() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/ouvertures-compte")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "11112222",
                                "typeCompteSouhaite", "FCP_PROGRES"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();

        MockMultipartFile cin = new MockMultipartFile("fichier", "cin.pdf", "application/pdf", "contenu".getBytes());
        mockMvc.perform(multipart("/api/ouvertures-compte/" + id + "/documents")
                        .file(cin).param("typeDocument", "CIN")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        List<DocumentJoint> documents = documentJointRepository
                .findByTypeOperationAndOperationId(TypeOperation.OUVERTURE_COMPTE, id);
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getImportePar()).isEqualTo("agent1");
    }
}
