import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UtilisateurAdmin, UtilisateurCreateRequest } from '../models/utilisateur.model';

/** Gestion des comptes utilisateurs du back-office, reservee au role ADMIN. */
@Injectable({ providedIn: 'root' })
export class UtilisateurAdminService {
  private readonly baseUrl = '/api/utilisateurs';

  constructor(private readonly http: HttpClient) {}

  lister(): Observable<UtilisateurAdmin[]> {
    return this.http.get<UtilisateurAdmin[]>(this.baseUrl);
  }

  creer(request: UtilisateurCreateRequest): Observable<UtilisateurAdmin> {
    return this.http.post<UtilisateurAdmin>(this.baseUrl, request);
  }

  changerStatut(id: number, actif: boolean): Observable<UtilisateurAdmin> {
    return this.http.patch<UtilisateurAdmin>(`${this.baseUrl}/${id}/statut`, { actif });
  }
}
