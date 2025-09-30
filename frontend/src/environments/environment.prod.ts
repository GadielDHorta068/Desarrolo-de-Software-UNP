// Configuración del entorno para producción
export const environment = {
  production: true,
  // En producción, usa URL relativa al mismo dominio donde está hosteado
  // Si el frontend está en https://raffyfy.argcloud.com.ar, la API será https://raffyfy.argcloud.com.ar:8080
  apiUrl: ':8080'
};
