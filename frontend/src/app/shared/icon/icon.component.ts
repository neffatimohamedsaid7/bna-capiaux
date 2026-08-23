import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

/**
 * Set d'icones "maison" (SVG en ligne, style trait, 24x24) - evite d'ajouter une
 * dependance externe (coherent avec la convention "pas de librairie de composants").
 * Chaque icone herite sa couleur de `currentColor` : elle suit le texte environnant.
 */
const ICONS: Record<string, string> = {
  souscription:
    '<path d="M7 3h7l5 5v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/><path d="M14 3v5h5"/><path d="M9 13h6"/><path d="M9 17h6"/><path d="M9 9h2"/>',
  rachat:
    '<circle cx="12" cy="12" r="9"/><path d="M12 7v10"/><path d="M15 10.5c0-1.4-1.3-2.5-3-2.5s-3 1-3 2.3c0 3 6 1.4 6 4.4 0 1.4-1.3 2.3-3 2.3s-3-1-3-2.3"/>',
  ouverture:
    '<path d="M3 7a2 2 0 0 1 2-2h9l4 4v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7z"/><path d="M13 12v5"/><path d="M10.5 14.5h5"/>',
  consultation:
    '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>',
  utilisateurs:
    '<circle cx="9" cy="8" r="3.2"/><path d="M3.5 19c0-3 2.5-5.2 5.5-5.2S14.5 16 14.5 19"/><circle cx="17" cy="9" r="2.5"/><path d="M16 13.3c2.4.2 4 2.2 4 4.7"/>',
  logout:
    '<path d="M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3"/><path d="M15 16l4-4-4-4"/><path d="M19 12H9"/>',
  'chevron-right': '<path d="M9 5l7 7-7 7"/>',
  'check-circle': '<circle cx="12" cy="12" r="9"/><path d="M8.5 12.3l2.4 2.4 4.6-5.2"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.3 2"/>',
  'x-circle': '<circle cx="12" cy="12" r="9"/><path d="M9 9l6 6"/><path d="M15 9l-6 6"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>',
  bank: '<path d="M3 21h18"/><path d="M4 21V10"/><path d="M20 21V10"/><path d="M2 10l10-6 10 6"/><path d="M8 21v-7"/><path d="M12 21v-7"/><path d="M16 21v-7"/>',
  'chart-bar': '<path d="M4 20V10"/><path d="M11 20V4"/><path d="M18 20v-7"/><path d="M3 20h18"/>',
  plus: '<path d="M12 5v14"/><path d="M5 12h14"/>',
  'trend-up': '<path d="M4 16l5.5-6 4 4L20 6"/><path d="M14 6h6v6"/>',
  paperclip:
    '<path d="M8 12.5l6.5-6.5a3 3 0 1 1 4.2 4.2L11 18a5 5 0 1 1-7-7l6.9-6.9"/>',
  alert: '<path d="M12 3.5l9.5 16.5H2.5L12 3.5z"/><path d="M12 10v4"/><path d="M12 17.2v.1"/>',
  folder: '<path d="M3 6.5A1.5 1.5 0 0 1 4.5 5H10l2 2h7.5A1.5 1.5 0 0 1 21 8.5v10A1.5 1.5 0 0 1 19.5 20h-15A1.5 1.5 0 0 1 3 18.5v-12z"/>',
  eye: '<path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/>',
  'eye-off':
    '<path d="M3 3l18 18"/><path d="M10.6 5.2A9.9 9.9 0 0 1 12 5c6.4 0 10 7 10 7a17.7 17.7 0 0 1-3.4 4.3"/><path d="M6.5 6.6C3.7 8.3 2 12 2 12s3.6 7 10 7a9.6 9.6 0 0 0 3.6-.7"/><path d="M9.5 10a3 3 0 0 0 4.2 4.2"/>',
};

@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg
      [attr.width]="size"
      [attr.height]="size"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      [attr.stroke-width]="strokeWidth"
      stroke-linecap="round"
      stroke-linejoin="round"
      [innerHTML]="path"
      aria-hidden="true"
    ></svg>
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        flex-shrink: 0;
        line-height: 0;
      }
    `,
  ],
})
export class IconComponent {
  @Input() name = 'chart-bar';
  @Input() size = 20;
  @Input() strokeWidth = 1.8;

  constructor(private readonly sanitizer: DomSanitizer) {}

  /**
   * Le contenu vient exclusivement de ICONS (constante figee dans ce fichier, jamais d'entree
   * utilisateur) : bypassSecurityTrustHtml est necessaire ici car le sanitizer HTML d'Angular
   * supprime sinon les elements SVG enfants (path/circle) d'un [innerHTML] sur une balise <svg>.
   */
  get path(): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(ICONS[this.name] ?? ICONS['chart-bar']);
  }
}
