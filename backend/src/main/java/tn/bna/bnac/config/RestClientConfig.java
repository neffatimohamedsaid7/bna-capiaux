package tn.bna.bnac.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tn.bna.bnac.ws.BnacWsProperties;

import java.time.Duration;

/**
 * RestTemplate dedie aux appels sortants vers BNAC (WS1 a WS4).
 * Timeout aligne sur la section 6.2 (performance : reponse API <= 2s, WS BNAC inclus).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate bnacRestTemplate(RestTemplateBuilder builder, BnacWsProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }
}
