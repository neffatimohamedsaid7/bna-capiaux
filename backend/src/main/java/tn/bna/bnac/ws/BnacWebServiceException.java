package tn.bna.bnac.ws;

/**
 * Erreur technique lors d'un appel a un web service BNAC (indisponibilite, timeout, erreur 5xx).
 * Distincte des rejets fonctionnels (ex : "montant superieur a la provision"), qui sont
 * representes par les exceptions du package {@code tn.bna.bnac.common.exception}.
 */
public class BnacWebServiceException extends RuntimeException {

    private final String webService;

    public BnacWebServiceException(String webService, String message, Throwable cause) {
        super(message, cause);
        this.webService = webService;
    }

    public String getWebService() {
        return webService;
    }
}
