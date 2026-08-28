import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RachatService } from '../../core/services/rachat.service';
import { RachatResponse } from '../../core/models/rachat.model';
import { TYPES_DOCUMENT, TypeDocument, libelleProduit } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { AuthService } from '../../core/services/auth.service';
import { ouvrirPdf } from '../../core/pdf-download.util';
import { AuditHistoriqueComponent } from '../../shared/audit-historique.component';
import { RiskBriefComponent } from '../../shared/risk-brief.component';

@Component({
  selector: 'app-rachat-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent, AuditHistoriqueComponent, RiskBriefComponent],
  templateUrl: './rachat-detail.component.html',
})
export class RachatDetailComponent implements OnInit {
  readonly rachat = signal<RachatResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly busy = signal(false);
  readonly typesDocument = TYPES_DOCUMENT;
  readonly libelleProduit = libelleProduit;

  typeDocumentChoisi: TypeDocument = 'BULLETIN_RACHAT_SIGNE';
  fichierChoisi: File | null = null;
  private id!: number;

  constructor(
    private readonly rachatService: RachatService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
  }

  private charger(): void {
    this.loading.set(true);
    this.rachatService.getDetail(this.id).subscribe({
      next: (r) => {
        this.rachat.set(r);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  onFichierChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fichierChoisi = input.files?.[0] ?? null;
  }

  importer(): void {
    if (!this.fichierChoisi) return;
    this.busy.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.rachatService.importerDocument(this.id, this.typeDocumentChoisi, this.fichierChoisi).subscribe({
      next: () => {
        this.successMessage.set('Document importé avec succès.');
        this.fichierChoisi = null;
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  valider(): void {
    this.busy.set(true);
    this.errorMessage.set(null);
    this.rachatService.valider(this.id).subscribe({
      next: (r) => {
        this.rachat.set(r);
        this.successMessage.set('Rachat validé (WS3).');
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  rejeter(): void {
    this.busy.set(true);
    this.errorMessage.set(null);
    this.rachatService.rejeter(this.id).subscribe({
      next: (r) => {
        this.rachat.set(r);
        this.successMessage.set('Rachat rejeté.');
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  supprimer(): void {
    if (!confirm('Supprimer ce rachat ?')) return;
    this.rachatService.supprimer(this.id).subscribe({
      next: () => this.router.navigate(['/rachat']),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerBulletin(): void {
    this.rachatService.telechargerBulletin(this.id).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerDecharge(): void {
    this.rachatService.telechargerDecharge(this.id).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
