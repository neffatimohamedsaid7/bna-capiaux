import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { AuditService } from '../core/services/audit.service';
import { AuditEntry, TYPE_ACTION_LABELS, TypeOperationAudit } from '../core/models/audit.model';

/** Historique des actions (PEC, modification, import, validation, rejet, appel WS) d'une operation. */
@Component({
  selector: 'app-audit-historique',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit-historique.component.html',
})
export class AuditHistoriqueComponent implements OnChanges {
  @Input({ required: true }) typeOperation!: TypeOperationAudit;
  @Input({ required: true }) operationId!: number;

  entrees: AuditEntry[] = [];
  chargement = true;
  readonly typeActionLabels = TYPE_ACTION_LABELS;

  constructor(private readonly auditService: AuditService) {}

  ngOnChanges(): void {
    if (!this.typeOperation || !this.operationId) return;
    this.chargement = true;
    this.auditService.historique(this.typeOperation, this.operationId).subscribe({
      next: (entrees) => {
        this.entrees = entrees;
        this.chargement = false;
      },
      error: () => {
        this.chargement = false;
      },
    });
  }
}
