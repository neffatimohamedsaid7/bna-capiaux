package tn.bna.bnac.ws;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tn.bna.bnac.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifie la mise en cache de WS1 (section 6.2 du cahier des charges "Mise en cache des donnees
 * de reference") : une recherche client peuple bien le cache {@code detailClientBnac}, cle par
 * critere de recherche, evitant les appels repetes en rafale pour le meme client.
 */
class BnacClientCacheTest extends IntegrationTestBase {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void rechercheClient_peupleLeCacheDetailClientBnacParCritere() throws Exception {
        Cache cache = cacheManager.getCache("detailClientBnac");
        cache.clear();

        assertThat(cache.get("12345678")).isNull();

        mockMvc.perform(get("/api/souscriptions/recherche-client").param("critere", "12345678")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        assertThat(cache.get("12345678")).isNotNull();
        // Un autre critere ne doit pas etre affecte par l'entree deja en cache.
        assertThat(cache.get("87654321")).isNull();

        mockMvc.perform(get("/api/souscriptions/recherche-client").param("critere", "87654321")
                        .header("Authorization", bearer(tokenAgent)))
                .andExpect(status().isOk());

        assertThat(cache.get("87654321")).isNotNull();
    }
}
