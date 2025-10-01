// Construye la URL de la API dinámicamente según el hostname actual.
// Ejemplos:
// - Si entras por localhost => http://localhost:8080
// - Si entras por raffy.com => http://raffy.com:8080
// En entornos sin window (SSR o pruebas), usa localhost por defecto.
function getApiBaseUrl(): string {
  const fallback = 'http://localhost:8080';
  try {
    if (typeof window !== 'undefined' && window.location) {
      const protocol = window.location.protocol || 'http:';
      const host = window.location.hostname || 'localhost';
      // Si el frontend está en HTTPS (Cloudflare/Proxy), evita usar :8080 sin TLS.
      // Usa el mismo origen (443) y deja que el proxy rote hacia el backend.
      if (protocol === 'https:') {
        return `${protocol}//${host}`;
      }
      // En HTTP local, usamos el backend en :8080 directamente.
      return `${protocol}//${host}:8080`;
    }
  } catch {
    // Ignorar errores y usar el fallback
  }
  return fallback;
}

export const environment = {
  production: false,
  apiUrl: getApiBaseUrl()
};