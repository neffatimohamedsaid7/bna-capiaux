import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConsultationClientResponse, ConsultationFiltres, RachatConsultationDto, SouscriptionConsultationDto } from '../models/consultation.model';

@Injectable({ providedIn: 'root' })
export class ConsultationService {
  private readonly baseUrl = '/api/consultation';

  constructor(private readonly http: HttpClient) {}

  getPortefeuille(critere: string): Observable<ConsultationClientResponse> {
    return this.http.get<ConsultationClientResponse>(`${this.baseUrl}/portefeuille`, {
      params: { critere },
    });
  }

  getSouscriptions(critere: string, filtres: ConsultationFiltres): Observable<SouscriptionConsultationDto[]> {
    return this.http.get<SouscriptionConsultationDto[]>(`${this.baseUrl}/souscriptions`, {
      params: this.buildParams(critere, filtres),
    });
  }

  getRachats(critere: string, filtres: ConsultationFiltres): Observable<RachatConsultationDto[]> {
    return this.http.get<RachatConsultationDto[]>(`${this.baseUrl}/rachats`, {
      params: this.buildParams(critere, filtres),
    });
  }

  private buildParams(critere: string, filtres: ConsultationFiltres): Record<string, string> {
    const params: Record<string, string> = { critere };
    if (filtres.produit) params['produit'] = filtres.produit;
    if (filtres.etat) params['etat'] = filtres.etat;
    if (filtres.dateDebut) params['dateDebut'] = filtres.dateDebut;
    if (filtres.dateFin) params['dateFin'] = filtres.dateFin;
    return params;
  }
}
