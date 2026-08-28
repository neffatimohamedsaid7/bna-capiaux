import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AssistantService } from '../../core/services/assistant.service';
import { ChatMessage } from '../../core/models/assistant.model';
import { extractErrorMessage } from '../../core/http-error.util';
import { IconComponent } from '../icon/icon.component';

/**
 * Assistant d'aide en ligne (fonctionnalite optionnelle, hors perimetre du cahier des charges) :
 * repond aux questions sur le fonctionnement de l'application (regles de gestion, workflow),
 * a partir d'une base de connaissance fixe cote backend - pas d'acces aux donnees metier.
 * Monte une seule fois dans app.component.html, disponible sur tout ecran authentifie.
 */
@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss',
})
export class ChatWidgetComponent {
  readonly open = signal(false);
  readonly messages = signal<ChatMessage[]>([]);
  readonly sending = signal(false);
  readonly errorMessage = signal<string | null>(null);

  draft = '';

  @ViewChild('scrollAnchor') private scrollAnchor?: ElementRef<HTMLDivElement>;

  constructor(private readonly assistantService: AssistantService) {}

  toggle(): void {
    this.open.update((v) => !v);
  }

  envoyer(): void {
    const contenu = this.draft.trim();
    if (!contenu || this.sending()) return;

    this.errorMessage.set(null);
    this.messages.update((liste) => [...liste, { role: 'user', content: contenu }]);
    this.draft = '';
    this.sending.set(true);
    this.scrollBas();

    this.assistantService.chat(this.messages()).subscribe({
      next: (r) => {
        this.messages.update((liste) => [...liste, { role: 'assistant', content: r.reply }]);
        this.sending.set(false);
        this.scrollBas();
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err));
        this.sending.set(false);
      },
    });
  }

  private scrollBas(): void {
    setTimeout(() => {
      this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
    });
  }
}
