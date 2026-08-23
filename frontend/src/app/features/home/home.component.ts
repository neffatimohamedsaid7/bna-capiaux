import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStatsResponse } from '../../core/models/dashboard.model';
import { extractErrorMessage } from '../../core/http-error.util';
import { IconComponent } from '../../shared/icon/icon.component';

type StatKey = 'souscriptionsEnCours' | 'rachatsEnCours' | 'ouverturesEnCours';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  readonly modules = [
    {
      path: '/souscription',
      title: "Souscription d'actions",
      description: "Rechercher un client, enregistrer une souscription et suivre les demandes en cours.",
      statKey: 'souscriptionsEnCours' as const,
      icon: 'souscription',
    },
    {
      path: '/rachat',
      title: 'Demande de rachat',
      description: 'Rechercher un client, enregistrer un rachat et suivre les demandes en cours.',
      statKey: 'rachatsEnCours' as const,
      icon: 'rachat',
    },
    {
      path: '/ouverture',
      title: 'Ouverture de compte titre',
      description: "Enregistrer une nouvelle demande d'ouverture de compte titre BNAC.",
      statKey: 'ouverturesEnCours' as const,
      icon: 'ouverture',
    },
    {
      path: '/consultation',
      title: 'Consultation',
      description: "Consulter le portefeuille d'un client et l'historique souscriptions / rachats.",
      statKey: null,
      icon: 'consultation',
    },
  ];

  /** Palette categorielle validee (CVD-safe) pour la mini-repartition du bandeau. */
  private readonly chartMeta: Record<StatKey, { label: string; icon: string; color: string }> = {
    souscriptionsEnCours: { label: 'Souscriptions', icon: 'souscription', color: '#199e70' },
    ouverturesEnCours: { label: 'Ouvertures', icon: 'ouverture', color: '#3987e5' },
    rachatsEnCours: { label: 'Rachats', icon: 'rachat', color: '#d95926' },
  };

  readonly stats = signal<DashboardStatsResponse | null>(null);
  readonly statsError = signal<string | null>(null);

  readonly chartRows = computed(() => {
    const s = this.stats();
    if (!s) return [];
    const values: Record<StatKey, number> = {
      souscriptionsEnCours: s.souscriptionsEnCours,
      ouverturesEnCours: s.ouverturesEnCours,
      rachatsEnCours: s.rachatsEnCours,
    };
    const max = Math.max(1, s.souscriptionsEnCours, s.rachatsEnCours, s.ouverturesEnCours);
    return (Object.keys(this.chartMeta) as StatKey[]).map((key) => ({
      key,
      value: values[key],
      widthPct: Math.round((values[key] / max) * 100),
      ...this.chartMeta[key],
    }));
  });

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getStats().subscribe({
      next: (s) => this.stats.set(s),
      error: (err) => this.statsError.set(extractErrorMessage(err)),
    });
  }

  statFor(key: StatKey | null): number | null {
    if (!key) return null;
    const s = this.stats();
    return s ? s[key] : null;
  }
}
