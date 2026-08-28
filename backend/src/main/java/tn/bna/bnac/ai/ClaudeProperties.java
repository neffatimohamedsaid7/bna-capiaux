package tn.bna.bnac.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Lie la section {@code claude.*} de application.yml (fonctionnalites IA optionnelles). */
@Component
@ConfigurationProperties(prefix = "claude")
@Getter
@Setter
public class ClaudeProperties {

    private String apiKey = "";
    private String model = "claude-opus-5";
}
