package tn.bna.bnac.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.bna.bnac.ws.BnacWsProperties;

import java.util.concurrent.TimeUnit;

/**
 * Cache des donnees de reference WS1 (liste produits, valeurs liquidatives - section 6.2 du
 * cahier des charges : "Mise en cache des donnees de reference"). TTL court et configurable
 * ({@code bnac.ws.cache-ttl-seconds}, defaut 60s) car ces donnees evoluent au jour le jour
 * cote BNAC ; ce n'est pas un cache de longue duree.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final BnacWsProperties bnacWsProperties;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("detailClientBnac");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(bnacWsProperties.getCacheTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(500));
        return cacheManager;
    }
}
