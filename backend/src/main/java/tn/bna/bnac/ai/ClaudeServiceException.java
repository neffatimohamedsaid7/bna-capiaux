package tn.bna.bnac.ai;

/**
 * Erreur lors d'un appel a l'API Claude (cle absente, erreur reseau, erreur de service) pour
 * les fonctionnalites IA optionnelles (resume de risque, assistant agent). Distincte de
 * {@link tn.bna.bnac.ws.BnacWebServiceException}, qui concerne l'interfacage BNAC.
 */
public class ClaudeServiceException extends RuntimeException {

    public ClaudeServiceException(String message) {
        super(message);
    }

    public ClaudeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
