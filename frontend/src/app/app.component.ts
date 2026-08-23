import { CommonModule } from '@angular/common';
import { Component, computed } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from './core/services/auth.service';
import { ROLE_LABELS } from './core/models/auth.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  readonly navLinks = [
    { path: '/souscription', label: 'Souscription' },
    { path: '/rachat', label: 'Rachat' },
    { path: '/ouverture', label: 'Ouverture de compte' },
    { path: '/consultation', label: 'Consultation' },
  ];

  readonly roleLabels = ROLE_LABELS;

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly isLoginPage = computed(() => this.url().startsWith('/login'));

  constructor(
    private readonly router: Router,
    readonly authService: AuthService,
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
