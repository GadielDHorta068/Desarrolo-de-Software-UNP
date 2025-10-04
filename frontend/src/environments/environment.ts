// En desarrollo, usamos same-origin con prefijo '/api' y el dev-server
// de Angular se encarga de la redirección al backend vía proxy.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};