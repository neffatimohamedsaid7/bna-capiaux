import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RachatService } from '../../core/services/rachat.service';
import { CompteBnaDto } from '../../core/models/referentiel.model';
import { PRODUITS_FINANCIERS } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';

@Component({
  selector: 'app-rachat-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './rachat-form.component.html',
})
export class RachatFormComponent implements OnInit {
  readonly produits = PRODUITS_FINANCIERS;
  readonly comptesCredit = signal<CompteBnaDto[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  editId: number | null = null;

  readonly form = this.fb.group({
    cinRneClient: ['', Validators.required],
    numeroCompteTitre: ['', Validators.required],
    produit: ['', Validators.required],
    nombreActionsAVendre: [null as number | null, [Validators.required, Validators.min(1)]],
    numeroCompteBnaCredit: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly rachatService: RachatService,
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
      this.chargerComptesCredit(cinRneClient);
    }
  }

  private chargerComptesCredit(cinRneClient: string): void {
    this.rachatService.rechercherClient(cinRneClient).subscribe({
      next: (res) => this.comptesCredit.set(res.comptesCredit),
      error: (err) => this.errorMessage.set(extractErrorMessage(err)),
    });
  }

  private chargerPourModification(id: number): void {
    this.loading.set(true);
    this.rachatService.getDetail(id).subscribe({
      next: (r) => {
        this.form.patchValue({
          cinRneClient: r.cinRneClient,
          numeroCompteTitre: r.numeroCompteTitre,
          produit: r.produit,
          nombreActionsAVendre: r.nombreActionsAVendre,
          numeroCompteBnaCredit: r.numeroCompteBnaCredit,
        });
        this.chargerComptesCredit(r.cinRneClient);
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
      nombreActionsAVendre: value.nombreActionsAVendre,
      numeroCompteBnaCredit: value.numeroCompteBnaCredit!,
    };

    const obs = this.editId
      ? this.rachatService.modifier(this.editId, request)
      : this.rachatService.creer(request);

    obs.subscribe({
      next: (res) => {
        this.saving.set(false);
        this.router.navigate(['/rachat', res.id]);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.saving.set(false);
      },
    });
  }
}
