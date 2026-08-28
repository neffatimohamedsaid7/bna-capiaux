package tn.bna.bnac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Resume genere par IA pour aider un validateur a revoir une operation avant de la traiter. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskBriefResponse {
    private String brief;
}
