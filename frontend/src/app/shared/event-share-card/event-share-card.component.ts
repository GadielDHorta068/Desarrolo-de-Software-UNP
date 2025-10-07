import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UrlService } from '../../services/url.service';

@Component({
  selector: 'app-event-share-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="share-card">
      <div class="share-card__header">
        <div class="share-card__title">{{ title || 'Invitación al evento' }}</div>
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
    .share-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; max-width: 480px; background: #fff; }
    .share-card__header { margin-bottom: 12px; }
    .share-card__title { font-size: 18px; font-weight: 600; }
    .share-card__subtitle { font-size: 14px; color: #6b7280; }
    .share-card__content { display: flex; gap: 16px; align-items: center; }
    .share-card__qr img { width: 160px; height: 160px; object-fit: contain; border: 1px solid #e5e7eb; border-radius: 4px; }
    .share-card__info { flex: 1; }
    .share-card__code { margin-bottom: 8px; }
    .share-card__link a { color: #2563eb; text-decoration: none; }
    .share-card__link a:hover { text-decoration: underline; }
    .share-card__actions { margin-top: 16px; display: flex; gap: 8px; }
    .share-card__actions button { padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 6px; background: #f9fafb; cursor: pointer; }
    .share-card__actions button:hover { background: #f3f4f6; }
    .share-card__actions button:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class EventShareCardComponent {
  @Input() shortcode!: string;
  @Input() qrBase64?: string;
  @Input() title?: string;
  @Input() description?: string;
  @Input() originalUrl?: string;

  constructor(private urlService: UrlService) {}

  get shortLink(): string {
    return this.urlService.buildShortLink(this.shortcode);
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
}