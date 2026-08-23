import { NgClass } from '@angular/common';
import { Component, Input } from '@angular/core';
import { StatutOperation, libelleStatut } from '../core/models/enums';
import { IconComponent } from './icon/icon.component';

@Component({
  selector: 'app-statut-badge',
  standalone: true,
  imports: [NgClass, IconComponent],
  template: `<span class="badge" [ngClass]="cssClass"><app-icon [name]="iconName" [size]="12" [strokeWidth]="2.4"></app-icon>{{ label }}</span>`,
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

  get iconName(): string {
    switch (this.statut) {
      case 'VALIDE':
        return 'check-circle';
      case 'REJETE':
        return 'x-circle';
      default:
        return 'clock';
    }
  }
}
