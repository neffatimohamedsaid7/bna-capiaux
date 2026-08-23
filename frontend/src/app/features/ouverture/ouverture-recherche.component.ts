import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OuvertureService } from '../../core/services/ouverture.service';
import { OuvertureCompteResponse, RechercheClientOuvertureResponse } from '../../core/models/ouverture.model';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { libelleProduit } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-ouverture-recherche',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent],
  templateUrl: './ouverture-recherche.component.html',
})
export class OuvertureRechercheComponent implements OnInit {
  critere = '';
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly resultat = signal<RechercheClientOuvertureResponse | null>(null);
  readonly enCours = signal<OuvertureCompteResponse[]>([]);
  readonly libelleProduit = libelleProduit;

  constructor(
    private readonly ouvertureService: OuvertureService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    const cinRneClient = this.route.snapshot.queryParamMap.get('cinRneClient');
    if (cinRneClient) {
      this.critere = cinRneClient;
      this.rechercher();
    }
  }

  rechercher(): void {
    if (!this.critere.trim()) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.resultat.set(null);
    this.enCours.set([]);

    this.ouvertureService.rechercherClient(this.critere.trim()).subscribe({
      next: (res) => {
        this.resultat.set(res);
        this.loading.set(false);
        if (res.ficheBna?.cinRne) {
          this.chargerEnCours(res.ficheBna.cinRne);
        }
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  private chargerEnCours(cinRneClient: string): void {
    this.ouvertureService.listerEnCours(cinRneClient).subscribe({
      next: (list) => this.enCours.set(list),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  nouvelleDemande(): void {
    const r = this.resultat();
    if (!r?.ficheBna) return;
    this.router.navigate(['/ouverture/nouveau'], {
      queryParams: { cinRneClient: r.ficheBna.cinRne },
    });
  }

  supprimer(demande: OuvertureCompteResponse): void {
    if (!confirm(`Supprimer la demande ${demande.numeroDemande} ?`)) return;
    this.ouvertureService.supprimer(demande.id).subscribe({
      next: () => this.chargerEnCours(demande.cinRneClient),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
