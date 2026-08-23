import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SouscriptionService } from '../../core/services/souscription.service';
import { CompteBnaDto } from '../../core/models/referentiel.model';
import { PRODUITS_FINANCIERS } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';

@Component({
  selector: 'app-souscription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './souscription-form.component.html',
})
export class SouscriptionFormComponent implements OnInit {
  readonly produits = PRODUITS_FINANCIERS;
  readonly comptesEligibles = signal<CompteBnaDto[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  editId: number | null = null;

  readonly form = this.fb.group({
    cinRneClient: ['', Validators.required],
    numeroCompteTitre: ['', Validators.required],
    produit: ['', Validators.required],
    nombreActionsASouscrire: [null as number | null, [Validators.required, Validators.min(1)]],
    numeroCompteBnaDebit: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly souscriptionService: SouscriptionService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.editId = Number(idParam);
      this.chargerPourModification(this.editId);
      return;
    }

    const qp = this.route.snapshot.queryParamMap;
    const cinRneClient = qp.get('cinRneClient') ?? '';
    this.form.patchValue({
      cinRneClient,
      numeroCompteTitre: qp.get('numeroCompteTitre') ?? '',
      produit: qp.get('produit') ?? '',
    });
    if (cinRneClient) {
      this.chargerComptesEligibles(cinRneClient);
    }
  }

  private chargerComptesEligibles(cinRneClient: string): void {
    this.souscriptionService.rechercherClient(cinRneClient).subscribe({
      next: (res) => this.comptesEligibles.set(res.comptesEligiblesDebit),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  private chargerPourModification(id: number): void {
    this.loading.set(true);
    this.souscriptionService.getDetail(id).subscribe({
      next: (s) => {
        this.form.patchValue({
          cinRneClient: s.cinRneClient,
          numeroCompteTitre: s.numeroCompteTitre,
          produit: s.produit,
          nombreActionsASouscrire: s.nombreActionsASouscrire,
          numeroCompteBnaDebit: s.numeroCompteBnaDebit,
        });
        this.chargerComptesEligibles(s.cinRneClient);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const request = {
      cinRneClient: value.cinRneClient!,
      numeroCompteTitre: value.numeroCompteTitre!,
      produit: value.produit as any,
      nombreActionsASouscrire: value.nombreActionsASouscrire,
      numeroCompteBnaDebit: value.numeroCompteBnaDebit!,
    };

    const obs = this.editId
      ? this.souscriptionService.modifier(this.editId, request)
      : this.souscriptionService.creer(request);

    obs.subscribe({
      next: (res) => {
        this.saving.set(false);
        this.router.navigate(['/souscription', res.id]);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.saving.set(false);
      },
    });
  }
}
