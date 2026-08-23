import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OuvertureService } from '../../core/services/ouverture.service';
import { OuvertureCompteResponse } from '../../core/models/ouverture.model';
import { TYPES_DOCUMENT, TypeDocument, libelleProduit } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';
import { AuthService } from '../../core/services/auth.service';
import { ouvrirPdf } from '../../core/pdf-download.util';
import { AuditHistoriqueComponent } from '../../shared/audit-historique.component';

@Component({
  selector: 'app-ouverture-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatutBadgeComponent, AuditHistoriqueComponent],
  templateUrl: './ouverture-detail.component.html',
})
export class OuvertureDetailComponent implements OnInit {
  readonly demande = signal<OuvertureCompteResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly busy = signal(false);
  readonly typesDocument = TYPES_DOCUMENT;
  readonly libelleProduit = libelleProduit;

  typeDocumentChoisi: TypeDocument = 'FORMULAIRE_COMPTE_BNAC';
  fichierChoisi: File | null = null;
  private id!: number;

  constructor(
    private readonly ouvertureService: OuvertureService,
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
    this.ouvertureService.getDetail(this.id).subscribe({
      next: (d) => {
        this.demande.set(d);
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
    this.ouvertureService.importerDocument(this.id, this.typeDocumentChoisi, this.fichierChoisi).subscribe({
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
    this.ouvertureService.valider(this.id).subscribe({
      next: (d) => {
        this.demande.set(d);
        this.successMessage.set('Demande validée (WS4).');
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
    this.ouvertureService.rejeter(this.id).subscribe({
      next: (d) => {
        this.demande.set(d);
        this.successMessage.set('Demande rejetée.');
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  supprimer(): void {
    if (!confirm('Supprimer cette demande ?')) return;
    this.ouvertureService.supprimer(this.id).subscribe({
      next: () => this.router.navigate(['/ouverture']),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  imprimerBulletin(): void {
    this.ouvertureService.telechargerBulletin(this.id).subscribe({
      next: (blob) => ouvrirPdf(blob),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
