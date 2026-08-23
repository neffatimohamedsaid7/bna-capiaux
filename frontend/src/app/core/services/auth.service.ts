import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, Role, UtilisateurCourant } from '../models/auth.model';

const STORAGE_KEY = 'bnac_auth_session';

interface StoredSession {
  token: string;
  user: UtilisateurCourant;
}

/**
 * Session d'authentification back-office (JWT). Le token est conserve dans le
 * navigateur (localStorage) pour survivre a un rechargement de page, et rejoue
 * dans le header Authorization par AuthInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = '/api/auth';

  private readonly session = signal<StoredSession | null>(this.readStoredSession());

  readonly currentUser = computed<UtilisateurCourant | null>(() => this.session()?.user ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);

  /** CHARGE_DE_DOSSIER : saisie/PEC (creer, modifier, supprimer une operation). ADMIN a acces complet. */
  readonly canSaisir = computed(() => this.hasAnyRole(['CHARGE_DE_DOSSIER', 'ADMIN']));

  /** VALIDATEUR : validation ou rejet des operations en cours. ADMIN a acces complet. */
  readonly canValider = computed(() => this.hasAnyRole(['VALIDATEUR', 'ADMIN']));

  readonly isAdmin = computed(() => this.hasAnyRole(['ADMIN']));

  constructor(private readonly http: HttpClient) {}

  get token(): string | null {
    return this.session()?.token ?? null;
  }

  hasAnyRole(roles: Role[]): boolean {
    const role = this.currentUser()?.role;
    return !!role && roles.includes(role);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => {
        const stored: StoredSession = {
          token: response.token,
          user: {
            username: response.username,
            nom: response.nom,
            prenom: response.prenom,
            role: response.role,
          },
        };
        this.session.set(stored);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
      }),
    );
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  private readStoredSession(): StoredSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as StoredSession) : null;
    } catch {
      return null;
    }
  }
}
