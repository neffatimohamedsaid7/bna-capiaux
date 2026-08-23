package tn.bna.bnac.ouverture;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Module 3 - Ouverture de compte titre : RG3.1 a RG3.3 et autorisation par role. */
class OuvertureCompteModuleTest extends IntegrationTestBase {

    @Test
    void rechercheClient_nonBna_estBloquee() throws Exception {
        // RG3.1 : seuls les clients BNA sont eligibles a l'ouverture d'un compte titre.
        mockMvc.perform(get("/api/ouvertures-compte/recherche-client")
                        .param("critere", "00000000")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechercheClient_bnaSansCompteTitre_activeLaProcedureDOuverture() throws Exception {
        mockMvc.perform(get("/api/ouvertures-compte/recherche-client")
                        .param("critere", "11112222")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possedeCompteTitre").value(false))
                .andExpect(jsonPath("$.ficheBna.nomPrenom").value("Sami Gharbi"));
    }

    @Test
    void creerEtValiderDemande_pourClientSansCompteBnac_transmetLesDonneesSignaletiquesViaWS4() throws Exception {
        // RG3.3 : pour un client sans compte BNAC, les donnees signaletiques BNA sont
        // transmises directement via WS4 lors de la validation, et un nouveau compte titre est genere.
        long id = idDe(creerDemandeValide("11112222"));

        MockMultipartFile formulaire = new MockMultipartFile("fichier", "formulaire.pdf", "application/pdf", "contenu".getBytes());
        MockMultipartFile cin = new MockMultipartFile("fichier", "cin.pdf", "application/pdf", "contenu".getBytes());
        mockMvc.perform(multipart("/api/ouvertures-compte/" + id + "/documents")
                        .file(formulaire).param("typeDocument", "FORMULAIRE_COMPTE_BNAC")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());
        mockMvc.perform(multipart("/api/ouvertures-compte/" + id + "/documents")
                        .file(cin).param("typeDocument", "CIN")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/ouvertures-compte/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"))
                .andExpect(jsonPath("$.referenceWs4").isNotEmpty())
                .andExpect(jsonPath("$.numeroCompteTitreGenere").isNotEmpty());
    }

    @Test
    void valider_sansPiecesJointes_estBloquee() throws Exception {
        long id = idDe(creerDemandeValide("11112222"));

        mockMvc.perform(post("/api/ouvertures-compte/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cycleDeVie_modifierUniquementEnCoursDEnregistrement() throws Exception {
        // RG3.2
        long id = idDe(creerDemandeValide("11112222"));

        mockMvc.perform(post("/api/ouvertures-compte/" + id + "/rejeter").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REJETE"));

        mockMvc.perform(put("/api/ouvertures-compte/" + id)
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "11112222",
                                "typeCompteSouhaite", "SICAV_BNA"))))
                .andExpect(status().isConflict());
    }

    @Test
    void role_validateurNePeutPasCreerDePEC() throws Exception {
        mockMvc.perform(post("/api/ouvertures-compte")
                        .header("Authorization", bearer(tokenValidateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "11112222",
                                "typeCompteSouhaite", "FCP_PROGRES"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void role_chargeDeDossierNePeutPasValider() throws Exception {
        long id = idDe(creerDemandeValide("11112222"));

        mockMvc.perform(post("/api/ouvertures-compte/" + id + "/valider").header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isForbidden());
    }

    // ---- Utilitaires ------------------------------------------------------------------------------

    private MvcResult creerDemandeValide(String cinRneClient) throws Exception {
        return mockMvc.perform(post("/api/ouvertures-compte")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", cinRneClient,
                                "typeCompteSouhaite", "FCP_PROGRES"))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long idDe(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
