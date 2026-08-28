package tn.bna.bnac.assistant;

import com.anthropic.models.messages.MessageParam;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tn.bna.bnac.ai.ClaudeChatClient;
import tn.bna.bnac.ai.ClaudeServiceException;
import tn.bna.bnac.dto.ChatMessageDto;
import tn.bna.bnac.dto.ChatRequest;
import tn.bna.bnac.dto.ChatResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Assistant d'aide en ligne pour les chargés de dossier / validateurs (fonctionnalite
 * optionnelle, hors perimetre du cahier des charges) : repond aux questions sur le
 * fonctionnement de l'application a partir d'une base de connaissance fixe et restreinte
 * (assistant/knowledge-base.md, condensee du cahier des charges et du manuel utilisateur),
 * pas d'acces aux donnees clients ni aux operations - une simple aide contextuelle.
 */
@Service
@RequiredArgsConstructor
public class AssistantService {

    /** Borne le nombre de tours conserves pour limiter le cout/latence d'une conversation longue. */
    private static final int MAX_MESSAGES_HISTORIQUE = 20;

    private final ClaudeChatClient claudeChatClient;
    private final String systemPrompt = chargerSystemPrompt();

    public ChatResponse repondre(ChatRequest request) {
        List<ChatMessageDto> historique = request.getMessages();
        if (historique.size() > MAX_MESSAGES_HISTORIQUE) {
            historique = historique.subList(historique.size() - MAX_MESSAGES_HISTORIQUE, historique.size());
        }

        List<MessageParam> messages = historique.stream()
                .map(m -> MessageParam.builder()
                        .role("assistant".equals(m.getRole()) ? MessageParam.Role.ASSISTANT : MessageParam.Role.USER)
                        .content(m.getContent())
                        .build())
                .toList();

        String reponse = claudeChatClient.demanderTexte(systemPrompt, messages, 500);
        return ChatResponse.builder().reply(reponse).build();
    }

    private String chargerSystemPrompt() {
        try {
            String baseConnaissance = new ClassPathResource("assistant/knowledge-base.md")
                    .getContentAsString(StandardCharsets.UTF_8);
            return """
                    Tu es l'assistant integre a l'application interne de BNA Capitaux (souscription,
                    rachat, ouverture de compte titre, consultation). Tu aides les chargés de
                    dossier et les validateurs a comprendre le fonctionnement de l'application et
                    les regles de gestion, a partir UNIQUEMENT de la base de connaissance ci-dessous.
                    Si une question sort de ce perimetre (donnees d'un client precis, action a
                    effectuer a leur place, sujet hors application), dis clairement que tu ne peux
                    pas y repondre et invite a contacter un collegue ou un administrateur. Reponds
                    en francais, de maniere concise et directe.

                    --- Base de connaissance ---
                    %s
                    """.formatted(baseConnaissance);
        } catch (IOException e) {
            throw new ClaudeServiceException("Impossible de charger la base de connaissance de l'assistant.", e);
        }
    }
}
