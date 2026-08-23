import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStatsResponse } from '../../core/models/dashboard.model';
import { extractErrorMessage } from '../../core/http-error.util';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
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
    },
    {
      path: '/rachat',
      title: 'Demande de rachat',
      description: 'Rechercher un client, enregistrer un rachat et suivre les demandes en cours.',
      statKey: 'rachatsEnCours' as const,
    },
    {
      path: '/ouverture',
      title: 'Ouverture de compte titre',
      description: "Enregistrer une nouvelle demande d'ouverture de compte titre BNAC.",
      statKey: 'ouverturesEnCours' as const,
    },
    {
      path: '/consultation',
      title: 'Consultation',
      description: "Consulter le portefeuille d'un client et l'historique souscriptions / rachats.",
      statKey: null,
    },
  ];

  readonly stats = signal<DashboardStatsResponse | null>(null);
  readonly statsError = signal<string | null>(null);

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getStats().subscribe({
      next: (s) => this.stats.set(s),
      error: (err) => this.statsError.set(extractErrorMessage(err)),
    });
  }

  statFor(key: 'souscriptionsEnCours' | 'rachatsEnCours' | 'ouverturesEnCours' | null): number | null {
    if (!key) return null;
    const s = this.stats();
    return s ? s[key] : null;
  }
}
