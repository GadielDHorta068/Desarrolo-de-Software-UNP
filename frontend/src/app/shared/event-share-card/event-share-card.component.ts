import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UrlService } from '../../services/url.service';
import { Input } from '@angular/core';

@Component({
  selector: 'app-event-share-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="share-card">
      <div class="share-card__header">
        <!-- <div class="share-card__title">{{ title || 'Invitación al evento' }}</div> -->
        <div class="share-card__subtitle" *ngIf="description">{{ description }}</div>
      </div>
      <div class="share-card__content">
        <div class="share-card__qr" *ngIf="qrBase64">
          <img [src]="qrDataUrl" alt="QR del evento" />
        </div>
        <div class="share-card__info">
          <div class="share-card__code">Código del evento: <strong>{{ shortcode }}</strong></div>
          <div class="share-card__link">
            <a [href]="shortLink" target="_blank" rel="noopener">{{ shortLink }}</a>
          </div>
        </div>
      </div>

      <div class="share-card__actions">
        <button type="button" (click)="share()">Compartir</button>
        <button type="button" (click)="copyLink()">Copiar link</button>
        <button type="button" (click)="downloadQr()" [disabled]="!qrBase64">Descargar QR</button>
      </div>
    </div>
  `,
  styles: [`
    /* Variables de color para soportar modo claro/oscuro */
    :host {
      --card-bg: #ffffff;
      --card-border: #e5e7eb; /* gray-200 */
      --text-secondary: #6b7280; /* gray-500 */
      --link-color: #2563eb; /* blue-600 */
      --btn-bg: #f9fafb; /* gray-50 */
      --btn-bg-hover: #f3f4f6; /* gray-100 */
      --btn-text: #111827; /* gray-900 */
    }
    :host-context(.dark) {
      --card-bg: #1f2937; /* gray-800 */
      --card-border: #374151; /* gray-700 */
      --text-secondary: #9ca3af; /* gray-300 */
      --link-color: #93c5fd; /* blue-300 */
      --btn-bg: #111827; /* gray-900 */
      --btn-bg-hover: #1f2937; /* gray-800 */
      --btn-text: #e5e7eb; /* gray-200 */
    }
    @media (prefers-color-scheme: dark) {
      :host {
        --card-bg: #1f2937;
        --card-border: #374151;
        --text-secondary: #9ca3af;
        --link-color: #93c5fd;
        --btn-bg: #111827;
        --btn-bg-hover: #1f2937;
        --btn-text: #e5e7eb;
      }
    }

    .share-card {
      border: 1px solid var(--card-border);
      border-radius: 8px;
      padding: 16px;
      max-width: 100%;
      background: var(--card-bg);
      transition: background-color 150ms ease, border-color 150ms ease;
    }
    .share-card__header { margin-bottom: 12px; }
    .share-card__title { font-size: 18px; font-weight: 600; }
    .share-card__subtitle { font-size: 14px; color: var(--text-secondary); }
    .share-card__content { display: flex; gap: 16px; align-items: flex-start; }
    .share-card__qr img {
      width: 240px;
      height: 240px;
      object-fit: contain;
      border: 1px solid var(--card-border);
      border-radius: 4px;
      background: var(--card-bg);
    }
    .share-card__info { flex: 1; }
    .share-card__code { margin-bottom: 8px; }
    .share-card__link a { color: var(--link-color); text-decoration: none; }
    .share-card__link a:hover { text-decoration: underline; }
    .share-card__actions { margin-top: 16px; display: flex; gap: 8px; }
    .share-card__actions button {
      padding: 8px 12px;
      border: 1px solid var(--card-border);
      border-radius: 6px;
      background: var(--btn-bg);
      color: var(--btn-text);
      cursor: pointer;
      transition: background-color 150ms ease, color 150ms ease, border-color 150ms ease;
    }
    .share-card__actions button:hover { background: var(--btn-bg-hover); }
    .share-card__actions button:disabled { opacity: 0.5; cursor: not-allowed; }

    /* Responsivo: en pantallas pequeñas apilar y ajustar tamaño */
    @media (max-width: 640px) {
      .share-card__content { flex-direction: column; align-items: center; text-align: center; }
      .share-card__qr img { width: 200px; height: 200px; }
    }
  `]
})
export class EventShareCardComponent implements OnInit {
  // Estado interno, sin recibir atributos externos
  shortcode: string = '';
  qrBase64?: string;
  title: string = 'Invitación al evento';
  description?: string = 'Comparte el enlace corto y su código QR';
  // originalUrl?: string;
  originalUrl?: string = "www.google.com";

  @Input() eventId?: number;
  constructor(private urlService: UrlService, private cdr: ChangeDetectorRef) {}

  get shortLink(): string {
    // Evitar mostrar un link vacío antes de recibir el shortcode
    return this.shortcode ? this.urlService.buildShortLink(this.shortcode) : '';
  }

  get qrDataUrl(): string {
    return this.qrBase64 ? this.urlService.toQrDataUrl(this.qrBase64) : '';
  }

  async share(): Promise<void> {
    const text = this.composeShareText();
    const url = this.shortLink;

    try {
      // Intentar compartir con imagen (Web Share API Level 2)
      if (this.qrBase64 && 'canShare' in navigator && 'share' in navigator) {
        const file = await this.dataUrlToFile(this.qrDataUrl, 'qr-evento.png');
        const shareData: any = { title: this.title || 'Invitación al evento', text, url };
        if ((navigator as any).canShare && (navigator as any).canShare({ files: [file] })) {
          shareData.files = [file];
        }
        await (navigator as any).share(shareData);
        return;
      }

      // Fallback: compartir sin archivos si está disponible
      if ('share' in navigator) {
        await (navigator as any).share({ title: this.title || 'Invitación al evento', text, url });
        return;
      }

      // Fallback final: copiar al portapapeles
      await (navigator as any).clipboard?.writeText(`${text}\n${url}`);
      alert('Información copiada al portapapeles');
    } catch (e) {
      console.error('Error al compartir:', e);
      try {
        await (navigator as any).clipboard?.writeText(`${text}\n${url}`);
        alert('Información copiada al portapapeles');
      } catch (_) {
        alert('No se pudo compartir ni copiar.');
      }
    }
  }

  copyLink(): void {
    const url = this.shortLink;
    (navigator as any).clipboard?.writeText(url).then(() => {
      alert('Link copiado al portapapeles');
    }).catch(() => alert('No se pudo copiar el link'));
  }

  downloadQr(): void {
    if (!this.qrDataUrl) return;
    const a = document.createElement('a');
    a.href = this.qrDataUrl;
    a.download = 'qr-evento.png';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  private composeShareText(): string {
    const base = this.title ? `Te invito al evento: ${this.title}` : 'Te invito a este evento';
    const code = this.shortcode ? `\nCódigo: ${this.shortcode}` : '';
    const extra = this.originalUrl ? `\nEnlace original: ${this.originalUrl}` : '';
    return `${base}${code}${extra}`.trim();
  }

  private async dataUrlToFile(dataUrl: string, filename: string): Promise<File> {
    const res = await fetch(dataUrl);
    const blob = await res.blob();
    return new File([blob], filename, { type: blob.type || 'image/png' });
  }

  ngOnInit(): void {
    if (!this.eventId) {
      return;
    }
    // Derivar la URL actual completa (ej: http://localhost:4200/evento/id/123)
    const href = (typeof window !== 'undefined' && window.location && window.location.href)
      ? window.location.href
      : '';

    // console.log("[link-url] => url generada: ", href);
    if (!href) {
      // Si no se puede obtener la URL (SSR/edge), no hacemos nada
      return;
    }

    this.originalUrl = href;

    // Enviar al backend para acortar y generar QR
    this.urlService.saveUrlForEvent(this.eventId ?? 0, href).subscribe({
      next: (resp) => {
        this.qrBase64 = resp.qr;
        this.shortcode = resp.url.shortcode;
        this.originalUrl = resp.url.originalUrl;
        // Forzar actualización de la vista en escenarios con OnPush en ancestros
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error guardando URL para compartir:', err);
      }
    });
  }
}