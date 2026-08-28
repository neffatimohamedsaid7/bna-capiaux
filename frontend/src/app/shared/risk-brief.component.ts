import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { RiskBriefService } from '../core/services/risk-brief.service';
import { TypeOperationAudit } from '../core/models/audit.model';
import { extractErrorMessage } from '../core/http-error.util';
import { IconComponent } from './icon/icon.component';

/**
 * Resume de risque genere par IA (fonctionnalite optionnelle, hors perimetre du cahier des
 * charges) pour aider un validateur a revoir une operation avant de la traiter.
 */
@Component({
  selector: 'app-risk-brief',
  standalone: true,
  imports: [CommonModule, IconComponent],
  templateUrl: './risk-brief.component.html',
})
export class RiskBriefComponent implements OnChanges {
  @Input({ required: true }) typeOperation!: TypeOperationAudit;
  @Input({ required: true }) operationId!: number;

  brief: string | null = null;
  chargement = true;
  erreur: string | null = null;

  constructor(private readonly riskBriefService: RiskBriefService) {}

  ngOnChanges(): void {
    if (!this.typeOperation || !this.operationId) return;
    this.generer();
  }

  generer(): void {
    this.chargement = true;
    this.erreur = null;
    this.brief = null;
    this.riskBriefService.generer(this.typeOperation, this.operationId).subscribe({
      next: (r) => {
        this.brief = r.brief;
        this.chargement = false;
      },
      error: (err) => {
        this.erreur = extractErrorMessage(err);
        this.chargement = false;
      },
    });
  }
}
