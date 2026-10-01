// Si la app se sirve directo desde el dev-server (:4200), se llama al backend
// en el puerto 8080 del mismo host. Detrás de Nginx (localhost:80 o un túnel
// como ngrok/Cloudflare) se usa same-origin y Nginx reenvía las rutas al backend.
const isDevServer = typeof window !== 'undefined' && window.location.port === '4200';

export const environment = {
  production: false,
  apiUrl: isDevServer ? `${window.location.protocol}//${window.location.hostname}:8080` : ''
};
