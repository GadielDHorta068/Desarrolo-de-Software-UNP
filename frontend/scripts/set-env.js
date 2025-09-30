#!/usr/bin/env node

// Script para configurar variables de entorno en Angular
const fs = require('fs');
const path = require('path');

// Lee la variable de entorno API_URL
const apiUrl = process.env.API_URL || 'localhost:8080';

// Genera el contenido del archivo environment.ts
const environmentContent = `// Configuración del entorno generada automáticamente
export const environment = {
  production: false,
  apiUrl: 'http://${apiUrl}'
};
`;

// Escribe el archivo environment.ts
const envPath = path.join(__dirname, '../src/environments/environment.ts');
fs.writeFileSync(envPath, environmentContent);

console.log(`✅ Configuración generada: API_URL = http://${apiUrl}`);
