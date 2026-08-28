package tn.bna.bnac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un message de la conversation avec l'assistant (role "user" ou "assistant"). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    @Pattern(regexp = "user|assistant", message = "le role doit etre 'user' ou 'assistant'")
    private String role;

    @NotBlank(message = "le contenu du message est obligatoire")
    private String content;
}
