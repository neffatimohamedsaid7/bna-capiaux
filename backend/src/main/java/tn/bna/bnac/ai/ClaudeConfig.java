package tn.bna.bnac.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Client Claude partage par les fonctionnalites IA (resume de risque, assistant agent).
 * La cle est lue depuis {@code claude.api-key} (variable d'environnement ANTHROPIC_API_KEY) ;
 * le bean se construit meme si elle est vide pour ne pas bloquer le demarrage de l'application -
 * seul un appel reel echoue alors, avec un message explicite (voir ClaudeServiceException).
 */
@Configuration
@RequiredArgsConstructor
public class ClaudeConfig {

    private final ClaudeProperties properties;

    @Bean
    public AnthropicClient anthropicClient() {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.getApiKey())
                .build();
    }
}
