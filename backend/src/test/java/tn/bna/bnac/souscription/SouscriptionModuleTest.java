package tn.bna.bnac.souscription;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Module 1 - Souscription d'actions : RG1.1 a RG1.6 et autorisation par role. */
class SouscriptionModuleTest extends IntegrationTestBase {

    @Test
    void rechercheClient_nonBna_estBloquee() throws Exception {
        // RG1.1 : seuls les clients BNA sont eligibles.
        mockMvc.perform(get("/api/souscriptions/recherche-client")
                        .param("critere", "00000000")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La personne recherchée n'est pas un client BNA"));
    }

    @Test
    void rechercheClient_bnaSansCompteTitre_neRetourneAucunProduit() throws Exception {
        // RG1.2 : la PEC est reservee aux clients disposant deja d'un compte titre BNAC.
        mockMvc.perform(get("/api/souscriptions/recherche-client")
                        .param("critere", "11112222")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possedeCompteTitre").value(false))
                .andExpect(jsonPath("$.produits").isEmpty());
    }

    @Test
    void rechercheClient_bnaAvecCompteTitre_retourneLaFicheEtLesProduits() throws Exception {
        mockMvc.perform(get("/api/souscriptions/recherche-client")
                        .param("critere", "12345678")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possedeCompteTitre").value(true))
                .andExpect(jsonPath("$.ficheBna.nomPrenom").value("Ahmed Ben Salah"))
                .andExpect(jsonPath("$.produits.length()").value(2));
    }

    @Test
    void creerSouscription_avecCompteNAppartenantPasAuClient_estRejetee() throws Exception {
        // RG1.3 : le compte de debit doit appartenir au client et etre d'un type eligible.
        mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 1,
                                "numeroCompteBnaDebit", "00000000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerSouscription_montantSuperieurALaProvision_estRejetee() throws Exception {
        // RG1.5 : compte 01300054321 (client 12345678) a une provision de 2500.000 ;
        // 30 actions x 125.500 = 3765.000 > 2500.000.
        mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 30,
                                "numeroCompteBnaDebit", "01300054321"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("depasse la provision")));
    }

    @Test
    void creerSouscription_valide_appliqueRG16EtStatutInitial() throws Exception {
        // RG1.6 : la date valeur comptable est la date de l'operation.
        MvcResult result = creerSouscriptionValide();
        String body = result.getResponse().getContentAsString();

        String dateSouscription = objectMapper.readTree(body).get("dateSouscription").asText();
        String dateValeurComptable = objectMapper.readTree(body).get("dateValeurComptable").asText();

        org.assertj.core.api.Assertions.assertThat(dateValeurComptable).isEqualTo(dateSouscription);
        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(body).get("statut").asText())
                .isEqualTo("EN_COURS_ENREGISTREMENT");
    }

    @Test
    void cycleDeVie_modifierEtSupprimerUniquementEnCoursDEnregistrement() throws Exception {
        // RG1.4
        long id = idDe(creerSouscriptionValide());

        mockMvc.perform(put("/api/souscriptions/" + id)
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 2,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreActionsASouscrire").value(2));

        mockMvc.perform(post("/api/souscriptions/" + id + "/rejeter").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REJETE"));

        // Une fois rejetee, la PEC ne peut plus etre modifiee (RG1.4) -> 409.
        mockMvc.perform(put("/api/souscriptions/" + id)
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 3,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isConflict());
    }

    @Test
    void supprimer_uneSouscriptionEnCours_reussit() throws Exception {
        long id = idDe(creerSouscriptionValide());

        mockMvc.perform(delete("/api/souscriptions/" + id).header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/souscriptions/" + id).header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isNotFound());
    }

    @Test
    void valider_sansPiecesJointes_estBloquee() throws Exception {
        long id = idDe(creerSouscriptionValide());

        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Import obligatoire")));
    }

    @Test
    void valider_avecPiecesJointes_appelleWS2EtPasseAuStatutValide() throws Exception {
        long id = idDe(creerSouscriptionValide());
        importerDocuments(id);

        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"))
                .andExpect(jsonPath("$.referenceWs2").isNotEmpty())
                .andExpect(jsonPath("$.nouveauNombreActions").isNumber());
    }

    @Test
    void valider_genereUneEcritureComptableDebitClientCreditProduit() throws Exception {
        long id = idDe(creerSouscriptionValide());
        importerDocuments(id);

        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/ecritures-comptables/SOUSCRIPTION/" + id).header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].compteDebit").value("01100012345"))
                .andExpect(jsonPath("$[0].compteCredit").value("FCP_PROGRES"))
                .andExpect(jsonPath("$[0].montant").isNumber());
    }

    @Test
    void role_chargeDeDossierNePeutPasValider() throws Exception {
        long id = idDe(creerSouscriptionValide());
        importerDocuments(id);

        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isForbidden());
    }

    @Test
    void role_validateurNePeutPasCreerDePEC() throws Exception {
        mockMvc.perform(post("/api/souscriptions")
                        .header("Authorization", bearer(tokenValidateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cinRneClient", "12345678",
                                "numeroCompteTitre", "4047/155",
                                "produit", "FCP_PROGRES",
                                "nombreActionsASouscrire", 1,
                                "numeroCompteBnaDebit", "01100012345"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void role_adminPeutCreerEtValider() throws Exception {
        long id = idDe(creerSouscriptionValide());
        importerDocuments(id);

        mockMvc.perform(post("/api/souscriptions/" + id + "/valider").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));
    }

    // ---- Utilitaires ------------------------------------------------------------------------------

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
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void importerDocuments(long id) throws Exception {
        MockMultipartFile ordreVirement = new MockMultipartFile("fichier", "ordre.pdf", "application/pdf", "contenu".getBytes());
        MockMultipartFile bulletinSigne = new MockMultipartFile("fichier", "bulletin.pdf", "application/pdf", "contenu".getBytes());

        mockMvc.perform(multipart("/api/souscriptions/" + id + "/documents")
                        .file(ordreVirement)
                        .param("typeDocument", "ORDRE_VIREMENT")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/souscriptions/" + id + "/documents")
                        .file(bulletinSigne)
                        .param("typeDocument", "BULLETIN_SOUSCRIPTION_SIGNE")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());
    }
}
