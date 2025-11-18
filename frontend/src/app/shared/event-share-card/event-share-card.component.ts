import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UrlService } from '../../services/url.service';
import { Input } from '@angular/core';

@Component({
  selector: 'app-event-share-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="share-card-modern">
      <div class="share-card-header">
        <div class="share-card-subtitle" *ngIf="description">{{ description }}</div>
      </div>
      <div class="share-card-content">
        <div class="qr-section" *ngIf="qrBase64">
          <div class="qr-container">
            <img [src]="qrDataUrl" alt="QR del evento" class="qr-image" />
          </div>
        </div>
        <div class="info-section">
          <div class="code-display">
            <span class="code-label">Código del evento:</span>
            <span class="code-value">{{ shortcode }}</span>
          </div>
          <div class="link-display">
            <a [href]="shortLink" target="_blank" rel="noopener" class="event-link">
              {{ shortLink }}
            </a>
          </div>
        </div>
      </div>

      <div class="share-card-actions">
        <button type="button" (click)="share()" class="action-btn primary">
          <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z"/>
          </svg>
          Compartir
        </button>
        <button type="button" (click)="copyLink()" class="action-btn secondary">
          <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/>
          </svg>
          Copiar link
        </button>
        <button type="button" (click)="downloadQr()" [disabled]="!qrBase64" class="action-btn tertiary">
          <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
          </svg>
          Descargar QR
        </button>
      </div>
    </div>
  `,
  styles: [`
    /* Modern Share Card Styles */
    :host {
      --card-bg: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
      --card-bg-dark: linear-gradient(135deg, #1f2937 0%, #111827 100%);
      --card-border: #e2e8f0;
      --card-border-dark: #374151;
      --text-primary: #1f2937;
      --text-primary-dark: #f9fafb;
      --text-secondary: #64748b;
      --text-secondary-dark: #9ca3af;
      --accent-color: #3b82f6;
      --accent-color-dark: #60a5fa;
      --success-color: #10b981;
      --success-color-dark: #34d399;
    }

    .share-card-modern {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 1rem;
      padding: 2rem;
      max-width: 100%;
      transition: all 0.3s ease;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }

    :host-context(.dark) .share-card-modern {
      background: var(--card-bg-dark);
      border-color: var(--card-border-dark);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
    }

    .share-card-modern:hover {
      transform: translateY(-2px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15), 0 10px 10px -5px rgba(0, 0, 0, 0.08);
    }

    .share-card-header {
      margin-bottom: 1.5rem;
    }

    .share-card-subtitle {
      font-size: 0.875rem;
      color: var(--text-secondary);
      font-weight: 500;
    }

    :host-context(.dark) .share-card-subtitle {
      color: var(--text-secondary-dark);
    }

    .share-card-content {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 2rem;
      align-items: center;
    }

    .qr-section {
      display: flex;
      justify-content: center;
    }

    .qr-container {
      background: white;
      padding: 1.5rem;
      border-radius: 0.75rem;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
    }

    .qr-container:hover {
      transform: scale(1.05);
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    }

    .qr-image {
      width: 180px;
      height: 180px;
      object-fit: contain;
      border-radius: 0.5rem;
    }

    .info-section {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .code-display {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .code-label {
      font-size: 0.875rem;
      color: var(--text-secondary);
      font-weight: 500;
    }

    .code-value {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--text-primary);
      font-family: 'Courier New', monospace;
      letter-spacing: 0.05em;
      background: linear-gradient(135deg, var(--accent-color), var(--success-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    :host-context(.dark) .code-label {
      color: var(--text-secondary-dark);
    }

    :host-context(.dark) .code-value {
      background: linear-gradient(135deg, var(--accent-color-dark), var(--success-color-dark));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .link-display {
      margin-top: 0.5rem;
    }

    .event-link {
      color: var(--accent-color);
      text-decoration: none;
      font-weight: 500;
      font-size: 0.875rem;
      transition: all 0.2s ease;
      display: inline-block;
      padding: 0.5rem 0;
    }

    .event-link:hover {
      color: var(--success-color);
      text-decoration: underline;
    }

    :host-context(.dark) .event-link {
      color: var(--accent-color-dark);
    }

    :host-context(.dark) .event-link:hover {
      color: var(--success-color-dark);
    }

    .share-card-actions {
      margin-top: 1.5rem;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
      gap: 0.75rem;
    }

    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      border: none;
      border-radius: 0.5rem;
      font-weight: 600;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s ease;
      text-align: center;
    }

    .btn-icon {
      width: 1rem;
      height: 1rem;
    }

    .action-btn.primary {
      background: linear-gradient(135deg, var(--accent-color), #2563eb);
      color: white;
      box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3), 0 2px 4px -1px rgba(59, 130, 246, 0.2);
    }

    .action-btn.primary:hover {
      background: linear-gradient(135deg, #2563eb, #1d4ed8);
      transform: translateY(-1px);
      box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.4), 0 4px 6px -2px rgba(59, 130, 246, 0.2);
    }

    .action-btn.secondary {
      background: white;
      color: var(--accent-color);
      border: 1px solid var(--accent-color);
    }

    .action-btn.secondary:hover {
      background: var(--accent-color);
      color: white;
    }

    .action-btn.tertiary {
      background: var(--success-color);
      color: white;
      box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3), 0 2px 4px -1px rgba(16, 185, 129, 0.2);
    }

    .action-btn.tertiary:hover {
      background: #059669;
      transform: translateY(-1px);
    }

    .action-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      transform: none !important;
    }

    /* Dark mode button adjustments */
    :host-context(.dark) .action-btn.secondary {
      background: transparent;
      color: var(--accent-color-dark);
      border-color: var(--accent-color-dark);
    }

    :host-context(.dark) .action-btn.secondary:hover {
      background: var(--accent-color-dark);
      color: white;
    }

    /* Responsivo: en pantallas pequeñas apilar y ajustar tamaño */
    @media (max-width: 768px) {
      .share-card-content {
        grid-template-columns: 1fr;
        text-align: center;
        gap: 1.5rem;
      }
      
      .share-card-actions {
        grid-template-columns: 1fr;
      }
      
      .qr-image {
        width: 160px;
        height: 160px;
      }
    }

    @media (max-width: 480px) {
      .share-card-modern {
        padding: 1.5rem;
      }
      
      .qr-image {
        width: 140px;
        height: 140px;
      }
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