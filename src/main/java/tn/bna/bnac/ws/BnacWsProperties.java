package tn.bna.bnac.ws;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Lie la section {@code bnac.ws.*} de application.yml. */
@Component
@ConfigurationProperties(prefix = "bnac.ws")
@Getter
@Setter
public class BnacWsProperties {

    private boolean stubMode = true;
    private String baseUrl;
    private int timeoutMs = 2000;
    private String ws1DetailClient;
    private String ws2Souscription;
    private String ws3DemandeRachat;
    private String ws4OuvertureCompte;
}
