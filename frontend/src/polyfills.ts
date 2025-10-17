// Polyfills globales para entorno navegador
// Asegura que librerías que esperan variables de Node funcionen en el browser

// 'global' (Node) -> 'window'/'globalThis' en navegador
(function(){
  const g: any = (typeof globalThis !== 'undefined') ? globalThis : (typeof window !== 'undefined' ? window : {});
  if (typeof (g as any).global === 'undefined') {
    (g as any).global = g;
  }

  // Opcional: define 'process.env' vacío para dependencias que lo consulten
  if (typeof (g as any).process === 'undefined') {
    (g as any).process = { env: {} };
  }
})();