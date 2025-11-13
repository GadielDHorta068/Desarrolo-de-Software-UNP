## Objetivo
Hacer funcional la sección de 2FA en `frontend/src/app/pages/settings/` agregando un controlador en el backend que orqueste las solicitudes hacia el microservicio 2FA y exponiendo endpoints limpios para el frontend.

## Arquitectura
- **Frontend** (Angular): llama a `http://localhost:8080/2fa/*` usando `AuthService` ya preparado.
- **Backend** (Spring Boot 8080): nuevo `TwoFactorController` + `TwoFactorService` que proxyea a `twofa` (`http://twofa:8080`) dentro de la red Docker.
- **Microservicio 2FA** (Spring Boot 8082 host, 8080 container): recibe operaciones reales (`/api/2fa/*`).

## API del Backend a exponer
- `POST /2fa/enable` body `{ username }` → devuelve `{ qrCode, recoveryCodes }`.
- `POST /2fa/verify` body `{ username, code }` → `{ verified: boolean }`.
- `POST /2fa/verify-recovery/{username}` body `text/plain` con el código → `{ verified: boolean }`.
- `POST /2fa/rotate/{username}` → `{ qrCode, recoveryCodes }`.
- `POST /2fa/disable/{username}` → `204` o `{ ok: true }`.
- `GET /2fa/status/{username}` → `{ username, twoFactorEnabled, createdAt }` (usa endpoint de 2FA `/api/users/{username}/status`).

## Implementación Backend
1. **Service HTTP (RestTemplate):**
   - Archivo nuevo `backend/src/main/java/com/desarrollo/raffy/business/services/TwoFactorService.java`.
   - Lee `TWOFA_API_URL` (default `http://twofa:8080`) y construye llamadas a:
     - `POST /api/2fa/enable`
     - `POST /api/2fa/verify`
     - `POST /api/2fa/verify-recovery/{username}` con `Content-Type: text/plain`
     - `POST /api/2fa/rotate/{username}`
     - `POST /api/2fa/disable/{username}`
     - `GET /api/users/{username}/status`
   - Reutiliza el `RestTemplate` provisto (`config/RestTemplateConfig.java:10-13`).

2. **Controlador REST:**
   - Archivo nuevo `backend/src/main/java/com/desarrollo/raffy/presenter/TwoFactorController.java` con `@RequestMapping("/2fa")`.
   - Endpoints arriba mapeados. Usa `Response.ok(...)` para respuestas (ver `backend/src/main/java/com/desarrollo/raffy/Response.java`).
   - Requiere autenticación (no se agregan a la lista de `permitAll` en `SecurityConfig.java:66-91`).

3. **DTOs:**
   - Nuevos en `backend/src/main/java/com/desarrollo/raffy/dto/twofa/`:
     - `Enable2FARequest { String username }`
     - `Enable2FAResponse { String qrCode; List<String> recoveryCodes }`
     - `Verify2FARequest { String username; String code }`
     - `Verify2FAResponse { boolean verified }`
   - El controlador serializa/deserializa con estos tipos.

4. **Configuración:**
   - `application.yml`: agregar bloque `twofa:` con `url: ${TWOFA_API_URL:http://twofa:8080}` para claridad, o leer directamente `TWOFA_API_URL` desde `@Value` como en `EvolutionService.java:21-25`.
   - `docker-compose.yml`: añadir `TWOFA_API_URL=http://twofa:8080` en el servicio `backend` (ya está la red compartida).

## Cambios Frontend
1. **AuthService (`frontend/src/app/services/auth.service.ts`):** Ya implementa:
   - `enable2FA`, `verify2FA`, `verifyRecovery2FA`, `rotate2FA`, `disable2FA` apuntando a `/2fa/*`.
   - Agregar método `get2FAStatus(username)` que haga `GET /2fa/status/{username}`.
2. **SettingsComponent (`frontend/src/app/pages/settings/settings.component.ts`):**
   - Actualizar `check2FAStatus()` para llamar `authService.get2FAStatus(...)` y setear `is2FAEnabled` según respuesta.
   - Usar los métodos existentes para habilitar, verificar, rotar y deshabilitar (ya están conectados).

## Seguridad
- Los endpoints `2fa/*` quedan protegidos por JWT del backend; el `AuthInterceptor` ya añade `Authorization` en Angular.
- No se exponen credenciales ni claves nuevas.

## Validación
- Levantar todo con Docker; confirmar `twofa` operativo en `http://localhost:8082/actuator/health`.
- Probar:
  - Generar QR: `POST http://localhost:8080/2fa/enable { username }`.
  - Verificar código TOTP: `POST http://localhost:8080/2fa/verify { username, code }`.
  - Rotar y deshabilitar.
  - Estado: `GET http://localhost:8080/2fa/status/{username}`.
- En UI, en Settings→Seguridad: generar QR, verificar código, ver `is2FAEnabled` actualizado.

## Entregables
- Nuevos archivos `TwoFactorService.java`, `TwoFactorController.java`, DTOs `twofa/*`.
- Ajuste opcional en `application.yml` y `docker-compose.yml` para `TWOFA_API_URL`.
- Pequeña modificación en `AuthService` y `SettingsComponent` para el estado.

## Referencias de código
- `backend/config/RestTemplateConfig.java:10-13`
- `backend/presenter/EvolutionController.java:24-81` (patrón de controlador externo)
- `backend/business/services/EvolutionService.java:21-61` (patrón de service externo)
- `backend/src/main/resources/application.yml:124-130` (patrón de config externa)
- `frontend/src/app/services/auth.service.ts:351-408` (métodos 2FA ya definidos)
- `frontend/src/app/pages/settings/settings.component.ts:407-541` (UI 2FA existente)
