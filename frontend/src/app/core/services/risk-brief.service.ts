import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RiskBriefResponse } from '../models/risk-brief.model';
import { TypeOperationAudit } from '../models/audit.model';

/** Resume de risque genere par IA pour aider a la validation d'une operation. */
@Injectable({ providedIn: 'root' })
export class RiskBriefService {
  private readonly baseUrl = '/api/risk-brief';

  constructor(private readonly http: HttpClient) {}

  generer(typeOperation: TypeOperationAudit, operationId: number): Observable<RiskBriefResponse> {
    return this.http.get<RiskBriefResponse>(`${this.baseUrl}/${typeOperation}/${operationId}`);
  }
}
