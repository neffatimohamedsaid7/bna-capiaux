import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditEntry, TypeOperationAudit } from '../models/audit.model';

/** Journal d'audit des operations (section 6.4 du cahier des charges). */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly baseUrl = '/api/audit';

  constructor(private readonly http: HttpClient) {}

  historique(typeOperation: TypeOperationAudit, operationId: number): Observable<AuditEntry[]> {
    return this.http.get<AuditEntry[]>(`${this.baseUrl}/${typeOperation}/${operationId}`);
  }
}
