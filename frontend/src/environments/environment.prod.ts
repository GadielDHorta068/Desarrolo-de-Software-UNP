// En producción detrás de Cloudflare/Nginx, usamos same-origin con prefijo '/api'.
// Nginx debe tener una regla que proxyee '/api' hacia el backend.
export const environment = {
  production: true,
  apiUrl: '/api'
};