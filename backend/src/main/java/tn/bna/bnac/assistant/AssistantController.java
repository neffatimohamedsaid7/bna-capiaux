package tn.bna.bnac.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.dto.ChatRequest;
import tn.bna.bnac.dto.ChatResponse;

/**
 * Assistant d'aide en ligne (fonctionnalite optionnelle, hors perimetre du cahier des charges).
 * Accessible a tout utilisateur authentifie, sans restriction de role (pure aide contextuelle,
 * pas d'acces aux donnees metier).
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@Tag(name = "Assistant (IA)", description = "Assistant d'aide en ligne pour les chargés de dossier et validateurs")
public class AssistantController {

    private final AssistantService assistantService;

    @Operation(summary = "Envoie un message a l'assistant et recoit sa reponse")
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return assistantService.repondre(request);
    }
}
