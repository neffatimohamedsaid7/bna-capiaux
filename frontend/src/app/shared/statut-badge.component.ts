import { NgClass } from '@angular/common';
import { Component, Input } from '@angular/core';
import { StatutOperation, libelleStatut } from '../core/models/enums';

@Component({
  selector: 'app-statut-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="badge" [ngClass]="cssClass">{{ label }}</span>`,
})
export class StatutBadgeComponent {
  @Input() statut: StatutOperation | null | undefined;

  get label(): string {
    return libelleStatut(this.statut);
  }

  get cssClass(): string {
    switch (this.statut) {
      case 'VALIDE':
        return 'badge-valide';
      case 'REJETE':
        return 'badge-rejete';
      default:
        return 'badge-en-cours';
    }
  }
}
