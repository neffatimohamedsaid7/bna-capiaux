package tn.bna.bnac.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Historique complet de la conversation envoye a chaque appel : l'assistant est sans etat cote
 * serveur (comme le reste de l'authentification JWT de cette application), le frontend renvoie
 * les messages precedents a chaque tour.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotEmpty(message = "au moins un message est requis")
    @Valid
    private List<ChatMessageDto> messages;
}
