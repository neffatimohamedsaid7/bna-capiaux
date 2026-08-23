package tn.bna.bnac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du module "Souscription et Rachat des Actions" (BNA - BNA Capitaux).
 * <p>
 * Expose les API REST metier (PEC + validation) pour :
 * - Module 1 : Souscription d'actions
 * - Module 2 : Demande de rachat
 * - Module 3 : Ouverture de compte titre
 * - Module 4 : Consultation et edition
 * <p>
 * et s'interface avec le systeme BNA Capitaux (BNAC) via les web services WS1 a WS4.
 */
@SpringBootApplication
public class BnacBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BnacBackendApplication.class, args);
    }
}
