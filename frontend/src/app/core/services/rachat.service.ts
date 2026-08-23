import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NouveauRachatRequest, RachatResponse, RechercheClientRachatResponse } from '../models/rachat.model';
import { TypeDocument } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class RachatService {
  private readonly baseUrl = '/api/rachats';

  constructor(private readonly http: HttpClient) {}

  rechercherClient(critere: string): Observable<RechercheClientRachatResponse> {
    return this.http.get<RechercheClientRachatResponse>(`${this.baseUrl}/recherche-client`, {
      params: { critere },
    });
  }

  creer(request: NouveauRachatRequest): Observable<RachatResponse> {
    return this.http.post<RachatResponse>(this.baseUrl, request);
  }

  modifier(id: number, request: NouveauRachatRequest): Observable<RachatResponse> {
    return this.http.put<RachatResponse>(`${this.baseUrl}/${id}`, request);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listerEnCours(cinRneClient: string): Observable<RachatResponse[]> {
    return this.http.get<RachatResponse[]>(`${this.baseUrl}/en-cours`, {
      params: { cinRneClient },
    });
  }

  getDetail(id: number): Observable<RachatResponse> {
    return this.http.get<RachatResponse>(`${this.baseUrl}/${id}`);
  }

  importerDocument(id: number, typeDocument: TypeDocument, fichier: File): Observable<unknown> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post(`${this.baseUrl}/${id}/documents`, formData, {
      params: { typeDocument },
    });
  }

  valider(id: number): Observable<RachatResponse> {
    return this.http.post<RachatResponse>(`${this.baseUrl}/${id}/valider`, {});
  }

  rejeter(id: number): Observable<RachatResponse> {
    return this.http.post<RachatResponse>(`${this.baseUrl}/${id}/rejeter`, {});
  }

  telechargerBulletin(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/bulletin`, { responseType: 'blob' });
  }

  telechargerDecharge(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/decharge`, { responseType: 'blob' });
  }

  telechargerListeProduits(critere: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/recherche-client/impression`, {
      params: { critere },
      responseType: 'blob',
    });
  }
}
