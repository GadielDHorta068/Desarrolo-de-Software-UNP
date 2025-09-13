import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ImageUploadResponse {
  success: boolean;
  message: string;
  imageBase64?: string;
  fileName?: string;
  fileSize?: number;
  mimeType?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private readonly API_URL = `${environment.apiUrl}/images`;

  constructor(private http: HttpClient) {}

  /**
   * Sube una imagen al servidor
   * @param file Archivo de imagen a subir
   * @returns Observable con la respuesta del servidor
   */
  uploadImage(file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ImageUploadResponse>(`${this.API_URL}/upload`, formData);
  }

  /**
   * Convierte un archivo a Base64
   * @param file Archivo a convertir
   * @returns Promise con el string Base64
   */
  convertToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        // Remover el prefijo data:image/...;base64,
        const base64 = result.split(',')[1];
        resolve(base64);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  /**
   * Valida si un archivo es una imagen válida
   * @param file Archivo a validar
   * @param maxSizeInMB Tamaño máximo en MB (default: 5)
   * @returns Objeto con resultado de validación
   */
  validateImage(file: File, maxSizeInMB: number = 5): { valid: boolean; error?: string } {
    // Validar tipo de archivo
    if (!file.type.startsWith('image/')) {
      return { valid: false, error: 'Por favor selecciona un archivo de imagen válido.' };
    }

    // Validar tamaño
    const maxSizeInBytes = maxSizeInMB * 1024 * 1024;
    if (file.size > maxSizeInBytes) {
      return { valid: false, error: `La imagen no puede ser mayor a ${maxSizeInMB}MB.` };
    }

    // Validar tipos específicos permitidos
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return { valid: false, error: 'Tipo de imagen no permitido. Use JPG, PNG, GIF o WebP.' };
    }

    return { valid: true };
  }

  /**
   * Crea una URL de preview para una imagen
   * @param file Archivo de imagen
   * @returns Promise con la URL de preview
   */
  createPreviewUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        resolve(e.target.result);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  /**
   * Convierte Base64 a URL de imagen para mostrar
   * @param base64 String Base64 de la imagen
   * @param mimeType Tipo MIME de la imagen (default: image/jpeg)
   * @returns URL de la imagen
   */
  base64ToImageUrl(base64: string, mimeType: string = 'image/jpeg'): string {
    return `data:${mimeType};base64,${base64}`;
  }
}