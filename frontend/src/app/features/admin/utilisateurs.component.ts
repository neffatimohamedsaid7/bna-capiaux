import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UtilisateurAdminService } from '../../core/services/utilisateur-admin.service';
import { UtilisateurAdmin, UtilisateurCreateRequest } from '../../core/models/utilisateur.model';
import { Role, ROLE_LABELS } from '../../core/models/auth.model';
import { extractErrorMessage } from '../../core/http-error.util';

const ROLES: Role[] = ['CHARGE_DE_DOSSIER', 'VALIDATEUR', 'ADMIN'];

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './utilisateurs.component.html',
})
export class UtilisateursComponent implements OnInit {
  readonly utilisateurs = signal<UtilisateurAdmin[]>([]);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly roles = ROLES;
  readonly roleLabels = ROLE_LABELS;

  formulaireOuvert = false;
  nouvelUtilisateur: UtilisateurCreateRequest = this.formulaireVide();

  constructor(private readonly utilisateurAdminService: UtilisateurAdminService) {}

  ngOnInit(): void {
    this.charger();
  }

  private charger(): void {
    this.loading.set(true);
    this.utilisateurAdminService.lister().subscribe({
      next: (list) => {
        this.utilisateurs.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  private formulaireVide(): UtilisateurCreateRequest {
    return { username: '', password: '', nom: '', prenom: '', email: '', role: 'CHARGE_DE_DOSSIER' };
  }

  ouvrirFormulaire(): void {
    this.nouvelUtilisateur = this.formulaireVide();
    this.errorMessage.set(null);
    this.formulaireOuvert = true;
  }

  annuler(): void {
    this.formulaireOuvert = false;
  }

  creer(): void {
    this.busy.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.utilisateurAdminService.creer(this.nouvelUtilisateur).subscribe({
      next: (u) => {
        this.utilisateurs.update((list) => [...list, u]);
        this.successMessage.set(`Compte ${u.username} cree.`);
        this.formulaireOuvert = false;
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  changerStatut(utilisateur: UtilisateurAdmin): void {
    this.busy.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.utilisateurAdminService.changerStatut(utilisateur.id, !utilisateur.actif).subscribe({
      next: (u) => {
        this.utilisateurs.update((list) => list.map((x) => (x.id === u.id ? u : x)));
        this.busy.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.busy.set(false);
      },
    });
  }
}
