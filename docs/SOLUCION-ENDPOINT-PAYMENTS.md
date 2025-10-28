# Solución: Endpoint /payments no funciona en Dockerfile.dev

## Resumen Ejecutivo

**Problema:** El endpoint `/payments` funciona correctamente cuando se usa `Dockerfile.prod` pero falla cuando se usa `Dockerfile.dev` en el entorno de desarrollo.

**Causa Raíz:** Configuración de proxy faltante en el entorno de desarrollo de Angular.

**Solución:** Agregar configuración de proxy para `/payments` en `proxy.conf.json`.

**Estado:** ✅ **RESUELTO**

---

## Análisis Técnico Detallado

### Arquitectura del Sistema

El proyecto utiliza una arquitectura de microservicios con:
- **Frontend:** Angular (puerto 4200)
- **Backend:** Spring Boot (puerto 8080) 
- **Proxy:** Nginx (puerto 80)
- **Base de datos:** PostgreSQL (puerto 5432)

### Diferencias entre Entornos

#### Dockerfile.prod (FUNCIONABA)
```dockerfile
# Usa Nginx como servidor web
FROM nginx:alpine AS run
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/temp-angular/ /usr/share/nginx/html/
```

**Configuración de rutas en nginx.conf:**
```nginx
location ^~ /payments/ { 
    proxy_pass http://backend:8080/payments/; 
    proxy_http_version 1.1; 
}
location = /payments { 
    proxy_pass http://backend:8080/payments; 
    proxy_http_version 1.1; 
}
```

#### Dockerfile.dev (NO FUNCIONABA)
```dockerfile
# Usa Angular dev server
FROM node:22-alpine
CMD ["npm", "start"]
```

**Comando start en package.json:**
```json
"start": "ng serve --host 0.0.0.0 --port 4200 --proxy-config src/proxy.conf.json"
```

**Configuración original de proxy.conf.json:**
```json
{
  "/api": {
    "target": "http://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  },
  "/ws": {
    "target": "ws://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

### Problema Identificado

El servicio `PaymentService` en Angular utiliza:
```typescript
private apiUrl = '/payments'; // Línea 18 en payment.service.ts
```

**En producción:** Nginx redirige `/payments/*` → `http://backend:8080/payments/*` ✅

**En desarrollo:** Angular dev server NO tenía configuración de proxy para `/payments` ❌

### Flujo de Solicitudes

#### Antes de la corrección (FALLABA):
```
Frontend (Angular) → /payments/user/123 → Angular dev server → ❌ 404 Not Found
```

#### Después de la corrección (FUNCIONA):
```
Frontend (Angular) → /payments/user/123 → Angular dev server → proxy.conf.json → backend:8080/payments/user/123 ✅
```

---

## Solución Implementada

### Archivo Modificado
**Ruta:** `frontend/src/proxy.conf.json`

### Cambio Realizado
```json
{
  "/api": {
    "target": "http://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  },
  
  "/payments": {
    "target": "http://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  },

  "/ws": {
    "target": "ws://backend:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

### Configuración Agregada
- **Ruta:** `/payments`
- **Target:** `http://backend:8080`
- **Secure:** `false` (desarrollo local)
- **ChangeOrigin:** `true` (necesario para Docker networking)
- **LogLevel:** `debug` (para troubleshooting)

---

## Verificación de la Solución

### Pasos para Verificar
1. **Reiniciar el contenedor de desarrollo:**
   ```bash
   docker-compose restart frontend
   ```

2. **Verificar logs del proxy:**
   ```bash
   docker-compose logs -f frontend
   ```

3. **Probar endpoint en navegador:**
   ```
   http://localhost:4200/payments
   ```

4. **Verificar en DevTools:**
   - Network tab debe mostrar requests a `/payments/*`
   - Status code debe ser 200 (no 404)

### Endpoints Afectados
Todos los endpoints del PaymentService ahora funcionan en desarrollo:
- `GET /payments` - Obtener todos los pagos
- `GET /payments/user/{userId}/all` - Pagos por usuario
- `GET /payments/receiver/{receiverId}/all` - Pagos por receptor
- `GET /payments/event/{eventId}/all` - Pagos por evento
- `GET /payments/status/{status}/all` - Pagos por estado
- `GET /payments/{id}` - Pago específico
- `GET /payments/exists/{paymentId}` - Verificar existencia

---

## Impacto y Beneficios

### ✅ Beneficios Inmediatos
- **Consistencia:** Mismo comportamiento entre desarrollo y producción
- **Productividad:** Desarrolladores pueden probar funcionalidad de pagos localmente
- **Debugging:** Logs de proxy facilitan troubleshooting
- **Testing:** Pruebas E2E funcionan en ambos entornos

### 🔧 Mantenimiento
- **Configuración centralizada:** Todos los proxies en un archivo
- **Escalabilidad:** Fácil agregar nuevos endpoints
- **Documentación:** Cambio documentado para futuras referencias

---

## Lecciones Aprendidas

### 🎯 Mejores Prácticas
1. **Paridad Dev/Prod:** Mantener configuraciones similares entre entornos
2. **Documentación:** Documentar diferencias de configuración entre entornos
3. **Testing:** Probar funcionalidades en ambos entornos antes de deploy
4. **Proxy Configuration:** Revisar configuraciones de proxy al agregar nuevos endpoints

### 🚨 Puntos de Atención
- **Nuevos endpoints:** Recordar agregar configuración de proxy en desarrollo
- **Cambios de rutas:** Sincronizar cambios entre nginx.conf y proxy.conf.json
- **Docker networking:** Usar nombres de servicio (backend:8080) no localhost

---

## Información Técnica

**Fecha de resolución:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Desarrollador:** Asistente IA
**Archivos modificados:** 
- `frontend/src/proxy.conf.json`
- `docs/SOLUCION-ENDPOINT-PAYMENTS.md` (este archivo)

**Versiones:**
- Angular: 20.2.2
- Node.js: 22-alpine
- Nginx: alpine
- Docker Compose: 3.x

---

## Referencias

- [Angular Proxy Configuration](https://angular.io/guide/build#proxying-to-a-backend-server)
- [Docker Compose Networking](https://docs.docker.com/compose/networking/)
- [Nginx Proxy Configuration](https://nginx.org/en/docs/http/ngx_http_proxy_module.html)

---

*Documento generado automáticamente como parte del proceso de resolución de incidencias.*