#!/usr/bin/env node

// Script para configurar variables de entorno en Angular
const fs = require('fs');
const path = require('path');

// Lee la variable de entorno API_URL
const apiUrl = process.env.API_URL || 'localhost:8080';

// Función para construir la URL correcta
function buildApiUrl(url) {
  // Si la URL ya tiene protocolo, la devuelve tal como está
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // Si es localhost, usa http
  if (url.includes('localhost') || url.includes('127.0.0.1')) {
    return `http://${url}`;
  }
  
  // Para otros dominios, usa https por defecto
  return `https://${url}`;
}

const finalApiUrl = buildApiUrl(apiUrl);

// Genera el contenido del archivo environment.ts
const environmentContent = `// Configuración del entorno generada automáticamente
export const environment = {
  production: false,
  apiUrl: '${finalApiUrl}'
};
`;

// Escribe el archivo environment.ts
const envPath = path.join(__dirname, '../src/environments/environment.ts');
fs.writeFileSync(envPath, environmentContent);

console.log(`✅ Configuración generada: API_URL = ${finalApiUrl}`);
