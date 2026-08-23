import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NouvelleSouscriptionRequest, RechercheClientSouscriptionResponse, SouscriptionResponse } from '../models/souscription.model';
import { TypeDocument } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class SouscriptionService {
  private readonly baseUrl = '/api/souscriptions';

  constructor(private readonly http: HttpClient) {}

  rechercherClient(critere: string): Observable<RechercheClientSouscriptionResponse> {
    return this.http.get<RechercheClientSouscriptionResponse>(`${this.baseUrl}/recherche-client`, {
      params: { critere },
    });
  }

  creer(request: NouvelleSouscriptionRequest): Observable<SouscriptionResponse> {
    return this.http.post<SouscriptionResponse>(this.baseUrl, request);
  }

  modifier(id: number, request: NouvelleSouscriptionRequest): Observable<SouscriptionResponse> {
    return this.http.put<SouscriptionResponse>(`${this.baseUrl}/${id}`, request);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listerEnCours(cinRneClient: string): Observable<SouscriptionResponse[]> {
    return this.http.get<SouscriptionResponse[]>(`${this.baseUrl}/en-cours`, {
      params: { cinRneClient },
    });
  }

  getDetail(id: number): Observable<SouscriptionResponse> {
    return this.http.get<SouscriptionResponse>(`${this.baseUrl}/${id}`);
  }

  importerDocument(id: number, typeDocument: TypeDocument, fichier: File): Observable<unknown> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post(`${this.baseUrl}/${id}/documents`, formData, {
      params: { typeDocument },
    });
  }

  valider(id: number): Observable<SouscriptionResponse> {
    return this.http.post<SouscriptionResponse>(`${this.baseUrl}/${id}/valider`, {});
  }

  rejeter(id: number): Observable<SouscriptionResponse> {
    return this.http.post<SouscriptionResponse>(`${this.baseUrl}/${id}/rejeter`, {});
  }

  telechargerBulletin(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/bulletin`, { responseType: 'blob' });
  }

  telechargerAvisOperation(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/avis-operation`, { responseType: 'blob' });
  }

  telechargerListeProduits(critere: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/recherche-client/impression`, {
      params: { critere },
      responseType: 'blob',
    });
  }
}
