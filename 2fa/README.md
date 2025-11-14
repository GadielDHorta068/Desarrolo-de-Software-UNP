## twofactorauth (API 2FA con Spring Boot)

API REST completa para autenticación de dos factores (2FA) con TOTP, gestión de usuarios y administración del sistema. Implementa generación de códigos QR, códigos de recuperación seguros y endpoints administrativos. No requiere frontend: es solo API.

### Stack
- Java 11+ (runtime 17 en Docker)
- Spring Boot 2.7.x (Web, Data JPA, Security, Validation, Actuator)
- H2 (dev) / PostgreSQL (prod)
- `dev.samstevens.totp` (TOTP/QR)
- OpenAPI/Swagger UI

### Seguridad y prácticas
- Secreto TOTP cifrado con AES-GCM (IV aleatorio + tag de autenticación).
- `username` único y validado.
- Códigos de recuperación guardados con hash BCrypt; solo se muestran una vez al habilitar.
- Endpoint de verificación de código de recuperación marca one-shot como usado.
- Validación de entrada con Bean Validation y manejo global de errores.
- Actuator con healthcheck para Docker.

### Requisitos locales
- JDK 11+ y Maven 3.8+ (o usa Docker para construir/ejecutar)

### Configuración (dev)
Variables de entorno recomendadas:
```bash
set ENCRYPTION_KEY=pon_una_clave_secreta
```
`src/main/resources/application.properties` ya está preparado para usar `ENCRYPTION_KEY` y levantar en 8080 con H2.

### Construir y ejecutar (local)
```bash
mvn clean package -DskipTests
java -jar target/twofactorauth-0.0.1-SNAPSHOT.jar
```
API en `http://localhost:8080`.

### Docker
```bash
docker build -t twofactorauth:latest .
docker run -e ENCRYPTION_KEY=tu_clave -p 8080:8080 twofactorauth:latest
```
Compose con PostgreSQL:
```bash
docker compose up -d
```

### Endpoints de Autenticación 2FA

**Gestión de 2FA:**
- **POST** `/api/2fa/enable`
  - Request: `{ "username": "usuario" }`
  - Response: `{ "qrCode": "data:image/png;base64,...", "recoveryCodes": ["...", ...] }`
  - Habilita 2FA para el usuario y genera códigos de recuperación

- **POST** `/api/2fa/verify`
  - Request: `{ "username": "usuario", "code": "123456" }`
  - Response: `{ "verified": true|false }`
  - Verifica código TOTP de 6 dígitos

- **POST** `/api/2fa/verify-recovery/{username}`
  - Body (text/plain o JSON string): código de recuperación
  - Response: `{ "verified": true|false }`
  - Verifica y marca como usado el código de recuperación

- **POST** `/api/2fa/rotate/{username}`
  - Regenera el secreto TOTP, devuelve nuevo `qrCode` y nuevos `recoveryCodes`
  - Útil para rotación periódica o en caso de compromiso

- **POST** `/api/2fa/disable/{username}`
  - Inhabilita 2FA: elimina el secreto y limpia códigos de recuperación
  - Respuesta 204 No Content

### Endpoints de Gestión de Usuarios

**Administración de Usuarios:**
- **GET** `/api/users/{username}/status`
  - Response: `{ "username": "usuario", "twoFactorEnabled": true|false, "createdAt": "2024-01-01T00:00:00" }`
  - Obtiene el estado de 2FA de un usuario específico

- **POST** `/api/users`
  - Request: `{ "username": "nuevo_usuario", "email": "usuario@ejemplo.com", "password": "SecurePass123!" }`
  - Response: `{ "username": "nuevo_usuario", "email": "usuario@ejemplo.com", "createdAt": "2024-01-01T00:00:00" }`
  - Crea un nuevo usuario en el sistema

- **GET** `/api/users/{username}`
  - Response: `{ "username": "usuario", "email": "usuario@ejemplo.com", "twoFactorEnabled": true|false, "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-01T00:00:00" }`
  - Obtiene información completa de un usuario

- **DELETE** `/api/users/{username}`
  - Elimina un usuario del sistema
  - Respuesta 204 No Content

### Endpoints Administrativos

**Estadísticas y Monitoreo:**
- **GET** `/api/admin/stats`
  - Response: `{ "totalUsers": 150, "usersWith2FA": 75, "twoFactorEnabledPercentage": 50.0 }`
  - Obtiene estadísticas generales del sistema

- **GET** `/api/admin/users`
  - Response: `[{ "username": "usuario1", "email": "user1@ejemplo.com", "twoFactorEnabled": true, "createdAt": "2024-01-01T00:00:00" }, ...]`
  - Lista todos los usuarios con información de 2FA

### Endpoints de Autenticación General

- **POST** `/api/auth/register`
  - Request: `{ "username": "usuario", "email": "usuario@ejemplo.com", "password": "SecurePass123!" }`
  - Registro de nuevos usuarios

- **POST** `/api/auth/login`
  - Request: `{ "username": "usuario", "password": "SecurePass123!" }`
  - Inicio de sesión con credenciales

Swagger UI: `http://localhost:8080/swagger-ui/index.html`
H2 Console (dev): `http://localhost:8080/h2-console`
Health: `http://localhost:8080/actuator/health`

### Rate limiting
- Los endpoints de verificación (`/verify` y `/verify-recovery/{username}`) tienen límite por `username` + IP (p. ej., 5 req/min). Si se supera, responde con 400/429.

### Producción (sugerencias)
- Usa PostgreSQL y desactiva H2/Swagger si no es necesario.
- Gestiona `ENCRYPTION_KEY` en un secret manager.
- Activa autenticación (p. ej., JWT) y CORS según tu caso. El `SecurityFilterChain` permite `/api/2fa/**` y `actuator/health` por defecto y deniega el resto.
- Añade rate limiting por usuario/IP para `/verify` (base `Bucket4j` incluida).
- Migraciones con Flyway/Liquibase; `ddl-auto=validate`.

### Uso de la API

#### Flujo de Autenticación con 2FA

1. **Registro de usuario:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "juan_perez", "email": "juan@ejemplo.com", "password": "SecurePass123!"}'
   ```

2. **Habilitar 2FA:**
   ```bash
   curl -X POST http://localhost:8080/api/2fa/enable \
     -H "Content-Type: application/json" \
     -d '{"username": "juan_perez"}'
   ```
   *Respuesta incluye QR code para escanear con app autenticadora y códigos de recuperación*

3. **Verificar código 2FA:**
   ```bash
   curl -X POST http://localhost:8080/api/2fa/verify \
     -H "Content-Type: application/json" \
     -d '{"username": "juan_perez", "code": "123456"}'
   ```

#### Gestión de Usuarios (Administrador)

4. **Ver estado de 2FA de un usuario:**
   ```bash
   curl -X GET http://localhost:8080/api/users/juan_perez/status
   ```

5. **Crear nuevo usuario (admin):**
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"username": "maria_gomez", "email": "maria@ejemplo.com", "password": "SecurePass456!"}'
   ```

6. **Obtener información completa de usuario:**
   ```bash
   curl -X GET http://localhost:8080/api/users/juan_perez
   ```

7. **Eliminar usuario:**
   ```bash
   curl -X DELETE http://localhost:8080/api/users/juan_perez
   ```

#### Estadísticas del Sistema

8. **Ver estadísticas generales:**
   ```bash
   curl -X GET http://localhost:8080/api/admin/stats
   ```

9. **Listar todos los usuarios con estado 2FA:**
   ```bash
   curl -X GET http://localhost:8080/api/admin/users
   ```

### Docker Compose - Despliegue Completo

1. **Configurar variables de entorno:**
   ```powershell
   # Windows PowerShell
   $env:ENCRYPTION_KEY = "clave_de_cifrado_super_segura_32_caracteres"
   ```
   
   ```bash
   # Linux/Mac
   export ENCRYPTION_KEY="clave_de_cifrado_super_segura_32_caracteres"
   ```

2. **Desplegar con Docker Compose:**
   ```bash
   docker-compose up -d --build
   ```

3. **Verificar estado de servicios:**
   ```bash
   docker-compose ps
   ```

4. **Ver logs de la aplicación:**
   ```bash
   docker-compose logs -f app
   ```

5. **Ejecutar pruebas automatizadas:**
   ```bash
   # Linux/Mac
   ./test-api-simple.sh
   
   # Windows PowerShell
   .\test-api-simple.ps1
   ```

6. **Detener servicios:**
   ```bash
   docker-compose down
   ```

### Docker - Construcción Manual

**Construir imagen Docker:**
```bash
docker build -t 2fa-autohosted:latest .
```

**Ejecutar contenedor individual:**
```bash
docker run -d \
  -e ENCRYPTION_KEY="clave_de_cifrado_super_segura" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -p 8080:8080 \
  --name 2fa-api \
  2fa-autohosted:latest
```

**Verificar health check:**
```bash
curl http://localhost:8080/api/health
```

### Documentación de API

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

**OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

**Health Check:** `http://localhost:8080/api/health`

**H2 Console (desarrollo):** `http://localhost:8080/h2-console`

### Seguridad

#### Cifrado y Protección de Datos
- **Secretos TOTP:** Cifrados con AES-GCM (256-bit) con IV aleatorio y tag de autenticación
- **Códigos de recuperación:** Hasheados con BCrypt (cost factor 12), nunca se almacenan en texto plano
- **Contraseñas:** Hasheadas con BCrypt
- **Validación de entrada:** Bean Validation con mensajes de error consistentes

#### Rate Limiting
- **Endpoints de verificación:** Límite de 5 intentos por minuto por usuario/IP
- **Respuesta al exceder límite:** HTTP 429 Too Many Requests
- **Implementación:** Bucket4j con almacenamiento en memoria

#### Configuración de Seguridad
- **CORS:** Configurado para permitir origines específicos
- **JWT:** Implementación de autenticación basada en tokens
- **Autorización:** Control de acceso basado en roles para endpoints administrativos

### Modelos de Datos

#### User (Usuario)
```json
{
  "username": "string",
  "email": "string", 
  "password": "string (hasheada)",
  "twoFactorEnabled": "boolean",
  "twoFactorSecret": "string (cifrada)",
  "recoveryCodes": "array[string] (hasheadas)",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

#### TwoFactorStatusResponse
```json
{
  "username": "string",
  "twoFactorEnabled": "boolean",
  "createdAt": "datetime"
}
```

#### SystemStatsResponse
```json
{
  "totalUsers": "integer",
  "usersWith2FA": "integer", 
  "twoFactorEnabledPercentage": "double"
}
```

### Códigos de Respuesta HTTP

- **200 OK:** Solicitud exitosa
- **201 Created:** Recurso creado exitosamente
- **204 No Content:** Operación exitosa sin contenido de respuesta
- **400 Bad Request:** Solicitud inválida o datos faltantes
- **401 Unauthorized:** No autenticado
- **403 Forbidden:** Sin permisos suficientes
- **404 Not Found:** Recurso no encontrado
- **429 Too Many Requests:** Rate limit excedido
- **500 Internal Server Error:** Error del servidor

### Notas de Diseño
- AES-GCM protege confidencialidad e integridad de secretos
- Recovery codes hasheados con BCrypt; verificación vía `matches`, nunca se re-muestran
- Respuestas de error consistentes via `@ControllerAdvice`
- Arquitectura hexagonal con separación clara entre capas
- Inyección de dependencias y principios SOLID aplicados
- Documentación automática con OpenAPI/Swagger