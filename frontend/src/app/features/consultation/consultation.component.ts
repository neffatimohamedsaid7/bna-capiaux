import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConsultationService } from '../../core/services/consultation.service';
import {
  ConsultationClientResponse,
  ConsultationFiltres,
  RachatConsultationDto,
  SouscriptionConsultationDto,
} from '../../core/models/consultation.model';
import { PRODUITS_FINANCIERS, STATUTS_OPERATION, libelleProduit } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';
import { StatutBadgeComponent } from '../../shared/statut-badge.component';

type Onglet = 'portefeuille' | 'souscriptions' | 'rachats';

@Component({
  selector: 'app-consultation',
  standalone: true,
  imports: [CommonModule, FormsModule, StatutBadgeComponent],
  templateUrl: './consultation.component.html',
})
export class ConsultationComponent {
  critere = '';
  onglet: Onglet = 'portefeuille';

  readonly produits = PRODUITS_FINANCIERS;
  readonly statuts = STATUTS_OPERATION;
  readonly libelleProduit = libelleProduit;

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly recherchee = signal(false);

  readonly portefeuille = signal<ConsultationClientResponse | null>(null);
  readonly souscriptions = signal<SouscriptionConsultationDto[]>([]);
  readonly rachats = signal<RachatConsultationDto[]>([]);

  filtres: ConsultationFiltres = { produit: '', etat: '', dateDebut: '', dateFin: '' };

  constructor(private readonly consultationService: ConsultationService) {}

  rechercher(): void {
    if (!this.critere.trim()) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.recherchee.set(true);

    this.consultationService.getPortefeuille(this.critere.trim()).subscribe({
      next: (res) => {
        this.portefeuille.set(res);
        this.loading.set(false);
        this.chargerSouscriptions();
        this.chargerRachats();
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.portefeuille.set(null);
        this.loading.set(false);
      },
    });
  }

  changerOnglet(o: Onglet): void {
    this.onglet = o;
  }

  appliquerFiltres(): void {
    this.chargerSouscriptions();
    this.chargerRachats();
  }

  private chargerSouscriptions(): void {
    this.consultationService.getSouscriptions(this.critere.trim(), this.filtres).subscribe({
      next: (list) => this.souscriptions.set(list),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  private chargerRachats(): void {
    this.consultationService.getRachats(this.critere.trim(), this.filtres).subscribe({
      next: (list) => this.rachats.set(list),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }
}
