import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SouscriptionService } from '../../core/services/souscription.service';
import { SouscriptionResponse } from '../../core/models/souscription.model';
import { TYPES_DOCUMENT, TypeDocument, libelleProduit } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { AuthService } from '../../core/services/auth.service';
import { ouvrirPdf } from '../../core/pdf-download.util';
import { AuditHistoriqueComponent } from '../../shared/audit-historique.component';

@Component({
  selector: 'app-souscription-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent, AuditHistoriqueComponent],
  templateUrl: './souscription-detail.component.html',
})
export class SouscriptionDetailComponent implements OnInit {
  readonly souscription = signal<SouscriptionResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly busy = signal(false);
  readonly typesDocument = TYPES_DOCUMENT;
  readonly libelleProduit = libelleProduit;

  typeDocumentChoisi: TypeDocument = 'ORDRE_VIREMENT';
  fichierChoisi: File | null = null;
  private id!: number;

  constructor(
    private readonly souscriptionService: SouscriptionService,
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
    this.souscriptionService.getDetail(this.id).subscribe({
      next: (s) => {
        this.souscription.set(s);
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
    this.souscriptionService.importerDocument(this.id, this.typeDocumentChoisi, this.fichierChoisi).subscribe({
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
    this.souscriptionService.valider(this.id).subscribe({
      next: (s) => {
        this.souscription.set(s);
        this.successMessage.set('Souscription validée (WS2).');
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
    this.souscriptionService.rejeter(this.id).subscribe({
      next: (s) => {
        this.souscription.set(s);
        this.successMessage.set('Souscription rejetée.');
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  supprimer(): void {
    if (!confirm('Supprimer cette souscription ?')) return;
    this.souscriptionService.supprimer(this.id).subscribe({
      next: () => this.router.navigate(['/souscription']),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerBulletin(): void {
    this.souscriptionService.telechargerBulletin(this.id).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerAvisOperation(): void {
    this.souscriptionService.telechargerAvisOperation(this.id).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
