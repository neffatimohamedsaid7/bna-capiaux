import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OuvertureService } from '../../core/services/ouverture.service';
import { PRODUITS_FINANCIERS } from '../../core/models/enums';
import { extractErrorMessage } from '../../core/http-error.util';

@Component({
  selector: 'app-ouverture-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ouverture-form.component.html',
})
export class OuvertureFormComponent implements OnInit {
  readonly produits = PRODUITS_FINANCIERS;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  editId: number | null = null;

  readonly form = this.fb.group({
    cinRneClient: ['', Validators.required],
    typeCompteSouhaite: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly ouvertureService: OuvertureService,
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
    this.form.patchValue({
      cinRneClient: qp.get('cinRneClient') ?? '',
    });
  }

  private chargerPourModification(id: number): void {
    this.loading.set(true);
    this.ouvertureService.getDetail(id).subscribe({
      next: (d) => {
        this.form.patchValue({
          cinRneClient: d.cinRneClient,
          typeCompteSouhaite: d.typeCompteSouhaite,
        });
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
      typeCompteSouhaite: value.typeCompteSouhaite as any,
    };

    const obs = this.editId
      ? this.ouvertureService.modifier(this.editId, request)
      : this.ouvertureService.creer(request);

    obs.subscribe({
      next: (res) => {
        this.saving.set(false);
        this.router.navigate(['/ouverture', res.id]);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.saving.set(false);
      },
    });
  }
}
