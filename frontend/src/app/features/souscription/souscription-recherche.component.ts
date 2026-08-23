import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SouscriptionService } from '../../core/services/souscription.service';
import { RechercheClientSouscriptionResponse, SouscriptionResponse } from '../../core/models/souscription.model';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { libelleProduit } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ouvrirPdf } from '../../core/pdf-download.util';

@Component({
  selector: 'app-souscription-recherche',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent],
  templateUrl: './souscription-recherche.component.html',
})
export class SouscriptionRechercheComponent {
  critere = '';
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly resultat = signal<RechercheClientSouscriptionResponse | null>(null);
  readonly enCours = signal<SouscriptionResponse[]>([]);
  readonly libelleProduit = libelleProduit;

  constructor(
    private readonly souscriptionService: SouscriptionService,
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

    this.souscriptionService.rechercherClient(this.critere.trim()).subscribe({
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
    this.souscriptionService.listerEnCours(cinRneClient).subscribe({
      next: (list) => this.enCours.set(list),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  souscrire(numeroCompteTitre: string, produit: string): void {
    const r = this.resultat();
    if (!r?.ficheBna) return;
    this.router.navigate(['/souscription/nouveau'], {
      queryParams: { cinRneClient: r.ficheBna.cinRne, numeroCompteTitre, produit },
    });
  }

  supprimer(souscription: SouscriptionResponse): void {
    if (!confirm(`Supprimer la souscription ${souscription.numeroSouscription} ?`)) return;
    this.souscriptionService.supprimer(souscription.id).subscribe({
      next: () => this.chargerEnCours(souscription.cinRneClient),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerListeProduits(): void {
    const r = this.resultat();
    if (!r?.ficheBna) return;
    this.souscriptionService.telechargerListeProduits(r.ficheBna.cinRne).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
