package tn.bna.bnac.consultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.bna.bnac.consultation.dto.ConsultationClientResponse;
import tn.bna.bnac.consultation.dto.RachatConsultationDto;
import tn.bna.bnac.consultation.dto.SouscriptionConsultationDto;
import tn.bna.bnac.domain.ProduitFinancier;
import tn.bna.bnac.domain.StatutOperation;

import java.time.LocalDate;
import java.util.List;

/**
 * Module 4 - Consultation et edition.
 * Portefeuille client (RG4.1/RG4.2) + vues Souscriptions / Rachats, cumulables et filtrables
 * (produit, etat, date de l'operation) comme decrit en section 4.2 du cahier des charges.
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@Tag(name = "Consultation", description = "Module 4 - Consultation et edition")
public class ConsultationController {

    private final ConsultationService consultationService;

    @Operation(summary = "Verifie le compte titre et affiche le portefeuille (fiche BNA + produits BNAC)")
    @GetMapping("/portefeuille")
    public ConsultationClientResponse getPortefeuille(@RequestParam String critere) {
        return consultationService.rechercherPortefeuille(critere);
    }

    @Operation(summary = "Vue Souscriptions (case a cocher), avec filtres produit/etat/date")
    @GetMapping("/souscriptions")
    public List<SouscriptionConsultationDto> getSouscriptions(
            @RequestParam String critere,
            @RequestParam(required = false) ProduitFinancier produit,
            @RequestParam(required = false) StatutOperation etat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return consultationService.consulterSouscriptions(critere, produit, etat, dateDebut, dateFin);
    }

    @Operation(summary = "Vue Rachats (case a cocher), avec filtres produit/etat/date")
    @GetMapping("/rachats")
    public List<RachatConsultationDto> getRachats(
            @RequestParam String critere,
            @RequestParam(required = false) ProduitFinancier produit,
            @RequestParam(required = false) StatutOperation etat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return consultationService.consulterRachats(critere, produit, etat, dateDebut, dateFin);
    }
}
