package tn.bna.bnac.riskbrief;

import com.anthropic.models.messages.MessageParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.bna.bnac.ai.ClaudeChatClient;
import tn.bna.bnac.audit.AuditService;
import tn.bna.bnac.common.exception.OperationIntrouvableException;
import tn.bna.bnac.domain.OuvertureCompte;
import tn.bna.bnac.domain.Rachat;
import tn.bna.bnac.domain.Souscription;
import tn.bna.bnac.domain.StatutOperation;
import tn.bna.bnac.domain.TypeOperation;
import tn.bna.bnac.dto.AuditEntryResponse;
import tn.bna.bnac.dto.CompteBnaDto;
import tn.bna.bnac.dto.RiskBriefResponse;
import tn.bna.bnac.referentiel.ClientBnaService;
import tn.bna.bnac.repository.OuvertureCompteRepository;
import tn.bna.bnac.repository.RachatRepository;
import tn.bna.bnac.repository.SouscriptionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Resume de risque genere par IA pour aider un VALIDATEUR a revoir une operation avant de la
 * valider ou la rejeter (fonctionnalite optionnelle, hors perimetre du cahier des charges).
 * Ne remplace aucune regle de gestion : celles-ci restent appliquees telles quelles par les
 * services metier (SouscriptionService, RachatService, OuvertureCompteService) - ce service ne
 * fait que resumer en langage naturel des faits deja calcules ou disponibles en base.
 */
@Service
@RequiredArgsConstructor
public class RiskBriefService {

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant qui aide un validateur de BNA Capitaux (banque tunisienne) a revoir
            rapidement une operation de souscription, rachat ou ouverture de compte titre avant de
            la valider ou la rejeter. A partir des faits fournis, redige un resume factuel de 1 a 3
            phrases en francais. Ne mentionne que des faits presents dans les donnees fournies,
            n'invente rien. Ne donne pas de recommandation explicite ("je recommande de...") : le
            validateur decide seul, tu te contentes de mettre en avant les elements utiles a sa
            decision (historique du client, montant par rapport a la provision, rejets anterieurs,
            actions recentes). Style concis, direct, professionnel, pas de formule de politesse.
            """;

    private final SouscriptionRepository souscriptionRepository;
    private final RachatRepository rachatRepository;
    private final OuvertureCompteRepository ouvertureCompteRepository;
    private final ClientBnaService clientBnaService;
    private final AuditService auditService;
    private final ClaudeChatClient claudeChatClient;

    public RiskBriefResponse genererResume(TypeOperation typeOperation, Long operationId) {
        String faits = switch (typeOperation) {
            case SOUSCRIPTION -> faitsSouscription(operationId);
            case RACHAT -> faitsRachat(operationId);
            case OUVERTURE_COMPTE -> faitsOuverture(operationId);
        };
        String brief = claudeChatClient.demanderTexte(SYSTEM_PROMPT,
                List.of(MessageParam.builder().role(MessageParam.Role.USER).content(faits).build()), 300);
        return RiskBriefResponse.builder().brief(brief).build();
    }

    private String faitsSouscription(Long id) {
        Souscription s = souscriptionRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
        List<Souscription> historique = souscriptionRepository.findByCinRneClient(s.getCinRneClient());

        long recentes7j = historique.stream()
                .filter(h -> !h.getDateSouscription().isBefore(LocalDate.now().minusDays(7)))
                .count();
        long rejets = historique.stream().filter(h -> h.getStatut() == StatutOperation.REJETE).count();

        String ratioProvision = clientBnaService.rechercherClient(s.getCinRneClient())
                .flatMap(client -> client.getComptes().stream()
                        .filter(c -> c.getNumeroCompte().equals(s.getNumeroCompteBnaDebit()))
                        .findFirst())
                .map(CompteBnaDto::getProvisionDisponible)
                .filter(provision -> provision.signum() > 0)
                .map(provision -> s.getMontantSouscription()
                        .divide(provision, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        + "% de la provision disponible")
                .orElse("inconnu (compte non retrouve dans le referentiel)");

        List<AuditEntryResponse> audit = auditService.historique(TypeOperation.SOUSCRIPTION, id);

        return """
                Operation : Souscription %s (statut actuel : %s)
                Client (CIN/RNE) : %s
                Produit : %s
                Montant demande : %s
                Montant / provision disponible sur le compte debite : %s
                Nombre total de souscriptions de ce client (tous statuts, historique complet) : %d
                Dont dans les 7 derniers jours : %d
                Dont rejets anterieurs : %d
                Dernieres actions du journal d'audit (les plus recentes en premier) : %s
                """.formatted(
                s.getNumeroSouscription(), s.getStatut(),
                s.getCinRneClient(),
                s.getProduit(),
                s.getMontantSouscription(),
                ratioProvision,
                historique.size(), recentes7j, rejets,
                formaterAudit(audit));
    }

    private String faitsRachat(Long id) {
        Rachat r = rachatRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
        List<Rachat> historique = rachatRepository.findByCinRneClient(r.getCinRneClient());

        long recents7j = historique.stream()
                .filter(h -> !h.getDateRachat().isBefore(LocalDate.now().minusDays(7)))
                .count();
        long rejets = historique.stream().filter(h -> h.getStatut() == StatutOperation.REJETE).count();

        List<AuditEntryResponse> audit = auditService.historique(TypeOperation.RACHAT, id);

        return """
                Operation : Rachat %s (statut actuel : %s)
                Client (CIN/RNE) : %s
                Produit : %s
                Nombre d'actions a vendre : %d
                Montant du rachat : %s
                Actions deja detenues avant ce rachat : %d
                Total des rachats deja en cours pour ce produit (cote BNA) : %d
                Total en attente d'approbation cote BNAC : %d
                Nombre total de rachats de ce client (tous statuts, historique complet) : %d
                Dont dans les 7 derniers jours : %d
                Dont rejets anterieurs : %d
                Dernieres actions du journal d'audit (les plus recentes en premier) : %s
                """.formatted(
                r.getNumeroRachat(), r.getStatut(),
                r.getCinRneClient(),
                r.getProduit(),
                r.getNombreActionsAVendre(),
                r.getMontantRachat(),
                r.getActionsEnProcessionAvantRachat(),
                r.getTotalRachatsEnCours(),
                r.getTotalRachatsEnAttenteApprobationBnac(),
                historique.size(), recents7j, rejets,
                formaterAudit(audit));
    }

    private String faitsOuverture(Long id) {
        OuvertureCompte o = ouvertureCompteRepository.findById(id)
                .orElseThrow(() -> new OperationIntrouvableException(id));
        List<OuvertureCompte> historique = ouvertureCompteRepository.findByCinRneClient(o.getCinRneClient());
        long rejets = historique.stream().filter(h -> h.getStatut() == StatutOperation.REJETE).count();

        List<AuditEntryResponse> audit = auditService.historique(TypeOperation.OUVERTURE_COMPTE, id);

        return """
                Operation : Ouverture de compte titre %s (statut actuel : %s)
                Client (CIN/RNE) : %s
                Type de compte souhaite : %s
                Nombre total de demandes d'ouverture de ce client (tous statuts, historique complet) : %d
                Dont rejets anterieurs : %d
                Dernieres actions du journal d'audit (les plus recentes en premier) : %s
                """.formatted(
                o.getNumeroDemande(), o.getStatut(),
                o.getCinRneClient(),
                o.getTypeCompteSouhaite(),
                historique.size(), rejets,
                formaterAudit(audit));
    }

    private String formaterAudit(List<AuditEntryResponse> audit) {
        if (audit.isEmpty()) {
            return "aucune";
        }
        return audit.stream()
                .limit(8)
                .map(e -> "[%s] %s par %s%s".formatted(
                        e.getDateAction(), e.getTypeAction(), e.getOperateur(),
                        e.getDetails() != null ? " (" + e.getDetails() + ")" : ""))
                .reduce((a, b) -> a + "; " + b)
                .orElse("aucune");
    }
}
