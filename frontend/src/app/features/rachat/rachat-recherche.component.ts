import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RachatService } from '../../core/services/rachat.service';
import { RachatResponse, RechercheClientRachatResponse } from '../../core/models/rachat.model';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { libelleProduit } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ouvrirPdf } from '../../core/pdf-download.util';

@Component({
  selector: 'app-rachat-recherche',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent],
  templateUrl: './rachat-recherche.component.html',
})
export class RachatRechercheComponent {
  critere = '';
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly resultat = signal<RechercheClientRachatResponse | null>(null);
  readonly enCours = signal<RachatResponse[]>([]);
  readonly libelleProduit = libelleProduit;

  constructor(
    private readonly rachatService: RachatService,
    private readonly router: Router,
    readonly authService: AuthService,
  ) {}

  rechercher(): void {
    if (!this.critere.trim()) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.resultat.set(null);
    this.enCours.set([]);

    this.rachatService.rechercherClient(this.critere.trim()).subscribe({
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
    this.rachatService.listerEnCours(cinRneClient).subscribe({
      next: (list) => this.enCours.set(list),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  demanderRachat(numeroCompteTitre: string, produit: string): void {
    const r = this.resultat();
    if (!r?.ficheBna) return;
    this.router.navigate(['/rachat/nouveau'], {
      queryParams: { cinRneClient: r.ficheBna.cinRne, numeroCompteTitre, produit },
    });
  }

  supprimer(rachat: RachatResponse): void {
    if (!confirm(`Supprimer le rachat ${rachat.numeroRachat} ?`)) return;
    this.rachatService.supprimer(rachat.id).subscribe({
      next: () => this.chargerEnCours(rachat.cinRneClient),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerListeProduits(): void {
    const r = this.resultat();
    if (!r?.ficheBna) return;
    this.rachatService.telechargerListeProduits(r.ficheBna.cinRne).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
