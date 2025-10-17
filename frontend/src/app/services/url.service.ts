import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SaveUrlResponse, Url } from '../models/url.model';

@Injectable({ providedIn: 'root' })
export class UrlService {
  private apiUrl = `${environment.apiUrl}/url`;

  constructor(private http: HttpClient) {}

  /**
   * Solicita al backend acortar un enlace y generar el QR.
   * Envía el URL original como texto plano en el body.
   */
  saveUrl(originalUrl: string): Observable<SaveUrlResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain' });
    const clean = this.sanitizeUrl(originalUrl);
    return this.http.post<SaveUrlResponse>(`${this.apiUrl}/save`, clean, { headers });
  }

  /**
   * Obtiene la entidad Url por shortcode.
   */
  getByShortcode(shortcode: string): Observable<Url> {
    return this.http.get<Url>(`${this.apiUrl}/${shortcode}`);
  }

  /**
   * Llama al endpoint de redirección. No sigue el 302, devuelve el response.
   */
  redirect(shortcode: string): Observable<HttpResponse<string>> {
    return this.http.get(`${this.apiUrl}/redirect/${shortcode}`, {
      observe: 'response',
      responseType: 'text'
    });
  }

  /**
   * Normaliza el Base64 del QR a data URL si el backend no lo incluye.
   */
  toQrDataUrl(qrBase64: string): string {
    return qrBase64.startsWith('data:image')
      ? qrBase64
      : `data:image/png;base64,${qrBase64}`;
  }

  /**
   * Construye el enlace corto público desde un shortcode.
   * Usa el origen actual para funcionar detrás del proxy.
   */
  buildShortLink(shortcode: string): string {
    const origin = (typeof window !== 'undefined' && window.location?.origin)
      ? window.location.origin
      : environment.apiUrl;
    // Ahora mostramos /invite/<shortcode> para que router/proxy lo resuelvan
    return `${origin}/invite/${shortcode}`;
  }

  /**
   * Sanitiza el URL de entrada para evitar comillas/backticks/\ al inicio/fin.
   * No modifica el contenido interno del URL, solo recorta y limpia bordes.
   */
  private sanitizeUrl(input: string): string {
    let s = (input ?? '').trim();
    // Elimina comillas simples, dobles, backticks y barras invertidas en los extremos
    s = s.replace(/^[`"'\\]+|[`"'\\]+$/g, '');
    return s.trim();
  }
}