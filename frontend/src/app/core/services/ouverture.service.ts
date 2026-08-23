import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NouvelleOuvertureRequest, OuvertureCompteResponse, RechercheClientOuvertureResponse } from '../models/ouverture.model';
import { TypeDocument } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class OuvertureService {
  private readonly baseUrl = '/api/ouvertures-compte';

  constructor(private readonly http: HttpClient) {}

  rechercherClient(critere: string): Observable<RechercheClientOuvertureResponse> {
    return this.http.get<RechercheClientOuvertureResponse>(`${this.baseUrl}/recherche-client`, {
      params: { critere },
    });
  }

  creer(request: NouvelleOuvertureRequest): Observable<OuvertureCompteResponse> {
    return this.http.post<OuvertureCompteResponse>(this.baseUrl, request);
  }

  modifier(id: number, request: NouvelleOuvertureRequest): Observable<OuvertureCompteResponse> {
    return this.http.put<OuvertureCompteResponse>(`${this.baseUrl}/${id}`, request);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listerEnCours(cinRneClient: string): Observable<OuvertureCompteResponse[]> {
    return this.http.get<OuvertureCompteResponse[]>(`${this.baseUrl}/en-cours`, {
      params: { cinRneClient },
    });
  }

  getDetail(id: number): Observable<OuvertureCompteResponse> {
    return this.http.get<OuvertureCompteResponse>(`${this.baseUrl}/${id}`);
  }

  importerDocument(id: number, typeDocument: TypeDocument, fichier: File): Observable<unknown> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post(`${this.baseUrl}/${id}/documents`, formData, {
      params: { typeDocument },
    });
  }

  valider(id: number): Observable<OuvertureCompteResponse> {
    return this.http.post<OuvertureCompteResponse>(`${this.baseUrl}/${id}/valider`, {});
  }

  rejeter(id: number): Observable<OuvertureCompteResponse> {
    return this.http.post<OuvertureCompteResponse>(`${this.baseUrl}/${id}/rejeter`, {});
  }

  telechargerBulletin(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/bulletin`, { responseType: 'blob' });
  }
}
