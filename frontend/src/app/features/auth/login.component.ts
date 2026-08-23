import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { extractErrorMessage } from '../../core/http-error.util';
import { IconComponent } from '../../shared/icon/icon.component';

interface Ripple {
  id: number;
  x: number;
  y: number;
  size: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, IconComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly afficherMotDePasse = signal(false);
  readonly shake = signal(false);
  readonly ripples = signal<Ripple[]>([]);

  /** Decalage de parallaxe normalise (-1..1) suivant la position de la souris dans l'ecran. */
  readonly parallax = signal({ x: 0, y: 0 });

  /** Attraction "magnetique" du bouton de connexion vers le curseur (en px, plafonnee). */
  readonly buttonMagnet = signal({ x: 0, y: 0 });

  private rippleSeq = 0;

  readonly form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.form.getRawValue();
    this.authService.login({ username: username!, password: password! }).subscribe({
      next: () => {
        this.loading.set(false);
        const redirect = this.route.snapshot.queryParamMap.get('redirect') ?? '/';
        this.router.navigateByUrl(redirect);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(extractErrorMessage(err));
        this.triggerShake();
      },
    });
  }

  onScreenMouseMove(event: MouseEvent): void {
    const x = (event.clientX / window.innerWidth) * 2 - 1;
    const y = (event.clientY / window.innerHeight) * 2 - 1;
    this.parallax.set({ x, y });
  }

  onButtonMouseMove(event: MouseEvent): void {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const cap = 8;
    const x = Math.max(-cap, Math.min(cap, (event.clientX - (rect.left + rect.width / 2)) * 0.25));
    const y = Math.max(-cap, Math.min(cap, (event.clientY - (rect.top + rect.height / 2)) * 0.35));
    this.buttonMagnet.set({ x, y });
  }

  onButtonMouseLeave(): void {
    this.buttonMagnet.set({ x: 0, y: 0 });
  }

  addRipple(event: MouseEvent): void {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height) * 1.6;
    const ripple: Ripple = {
      id: ++this.rippleSeq,
      x: event.clientX - rect.left - size / 2,
      y: event.clientY - rect.top - size / 2,
      size,
    };
    this.ripples.update((list) => [...list, ripple]);
    setTimeout(() => {
      this.ripples.update((list) => list.filter((r) => r.id !== ripple.id));
    }, 650);
  }

  private triggerShake(): void {
    this.shake.set(false);
    // Force un reflow pour permettre de rejouer l'animation meme sur des echecs consecutifs.
    requestAnimationFrame(() => {
      this.shake.set(true);
      setTimeout(() => this.shake.set(false), 500);
    });
  }
}
