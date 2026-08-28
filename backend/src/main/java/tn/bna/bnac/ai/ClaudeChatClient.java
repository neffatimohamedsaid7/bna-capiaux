package tn.bna.bnac.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Point d'entree partage vers Claude pour les fonctionnalites IA optionnelles (resume de risque,
 * assistant agent) : centralise la verification de la cle API et la traduction des erreurs en
 * {@link ClaudeServiceException}, pour que chaque appelant n'ait qu'a fournir le prompt.
 */
@Component
@RequiredArgsConstructor
public class ClaudeChatClient {

    private final AnthropicClient anthropicClient;
    private final ClaudeProperties properties;

    public String demanderTexte(String systemPrompt, List<MessageParam> messages, long maxTokens) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new ClaudeServiceException(
                    "Fonctionnalite IA non configuree : la variable d'environnement ANTHROPIC_API_KEY est absente.");
        }
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(properties.getModel())
                    .maxTokens(maxTokens)
                    .system(systemPrompt)
                    .messages(messages)
                    .build();
            Message response = anthropicClient.messages().create(params);
            return response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(TextBlock::text)
                    .collect(Collectors.joining())
                    .trim();
        } catch (AnthropicServiceException e) {
            throw new ClaudeServiceException("Le service IA a renvoye une erreur.", e);
        } catch (RuntimeException e) {
            throw new ClaudeServiceException("Impossible de contacter le service IA.", e);
        }
    }
}
