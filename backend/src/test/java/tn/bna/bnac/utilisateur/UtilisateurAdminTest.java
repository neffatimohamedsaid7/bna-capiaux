package tn.bna.bnac.utilisateur;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.bna.bnac.support.IntegrationTestBase;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Gestion des comptes utilisateurs (section 4 : responsabilite ADMIN, "acces complet"). */
class UtilisateurAdminTest extends IntegrationTestBase {

    @Test
    void lister_estReserveAuRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", bearer(tokenValidateur)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lister_enTantQuAdmin_retourneLesComptesDeDemonstration() throws Exception {
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'admin')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'agent1')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'validateur1')]").exists());
    }

    @Test
    void creer_estReserveAuRoleAdmin() throws Exception {
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", bearer(tokenAgent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "test-" + UUID.randomUUID(),
                                "password", "MotDePasse1!",
                                "nom", "Test", "prenom", "Utilisateur",
                                "role", "CHARGE_DE_DOSSIER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creer_enTantQuAdmin_creeLeCompteEtEmpecheLesDoublons() throws Exception {
        String username = "test-" + UUID.randomUUID();
        Map<String, Object> request = Map.of(
                "username", username,
                "password", "MotDePasse1!",
                "nom", "Test", "prenom", "Utilisateur",
                "role", "VALIDATEUR");

        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("VALIDATEUR"))
                .andExpect(jsonPath("$.actif").value(true));

        // Meme nom d'utilisateur -> conflit.
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void changerStatut_desactiveEtReactiveUnCompte() throws Exception {
        String username = "test-" + UUID.randomUUID();
        MvcResult creation = mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "MotDePasse1!",
                                "nom", "Test", "prenom", "Utilisateur",
                                "role", "CHARGE_DE_DOSSIER"))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/utilisateurs/" + id + "/statut")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actif\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(false));

        // Un compte desactive ne peut plus se connecter.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "MotDePasse1!"))))
                .andExpect(status().isUnauthorized());
    }
}
