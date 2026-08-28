import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatMessage, ChatResponse } from '../models/assistant.model';

/** Assistant d'aide en ligne (fonctionnalite optionnelle, hors perimetre du cahier des charges). */
@Injectable({ providedIn: 'root' })
export class AssistantService {
  private readonly baseUrl = '/api/assistant';

  constructor(private readonly http: HttpClient) {}

  chat(messages: ChatMessage[]): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl}/chat`, { messages });
  }
}
