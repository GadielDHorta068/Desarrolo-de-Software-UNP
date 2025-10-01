// Produce una URL de API dinámica en producción basada en el hostname actual.
// Si no hay acceso a window (SSR), usa el servicio interno "backend".
function getApiBaseUrlProd(): string {
  const internal = 'http://backend:8080';
  try {
    if (typeof window !== 'undefined' && window.location) {
      const protocol = window.location.protocol || 'http:';
      const host = window.location.hostname || 'localhost';
      return `${protocol}//${host}:8080`;
    }
  } catch {
    // Ignorar errores y usar el valor interno
  }
  return internal;
}

export const environment = {
  production: true,
  apiUrl: getApiBaseUrlProd()
};