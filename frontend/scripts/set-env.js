#!/usr/bin/env node

// Script para configurar variables de entorno en Angular
const fs = require('fs');
const path = require('path');

// Lee la variable de entorno API_URL
const apiUrl = process.env.API_URL || 'localhost:8080';

// Determina si es producción o desarrollo
const isProduction = process.env.NODE_ENV === 'production';

// Genera la URL de la API basada en el entorno
let finalApiUrl;
if (isProduction) {
  // En producción, usa la URL relativa del servidor donde está hosteado
  // Si el frontend está en https://raffyfy.argcloud.com.ar, la API será https://raffyfy.argcloud.com.ar:8080
  finalApiUrl = `:8080`; // URL relativa al mismo dominio
} else {
  // En desarrollo, usa localhost
  finalApiUrl = `http://${apiUrl}`;
}

// Genera el contenido del archivo environment.ts
const environmentContent = `// Configuración del entorno generada automáticamente
export const environment = {
  production: ${isProduction},
  apiUrl: '${finalApiUrl}'
};
`;

// Escribe el archivo environment.ts
const envPath = path.join(__dirname, '../src/environments/environment.ts');
fs.writeFileSync(envPath, environmentContent);

console.log(`✅ Configuración generada: API_URL = ${finalApiUrl} (${isProduction ? 'producción' : 'desarrollo'})`);
