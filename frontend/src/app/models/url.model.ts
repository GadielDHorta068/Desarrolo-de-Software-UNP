export interface Url {
  id: number;
  clickCount: number;
  shortcode: string;
  originalUrl: string;
  // El backend puede serializar LocalDateTime como arreglo [y,m,d,h,m,s,nan]
  // o como string; mantenemos flexible el tipo aquí.
  createdAt: string | number[];
}

export interface SaveUrlResponse {
  // Base64 del QR; puede venir con o sin prefijo data:image/png
  qr: string;
  url: Url;
}