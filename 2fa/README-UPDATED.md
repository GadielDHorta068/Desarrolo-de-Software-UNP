# 2FA Autohosted - API REST

API completa de autenticación de dos factores (2FA) con gestión de usuarios, estadísticas y pruebas automatizadas.

## 🚀 Características

- ✅ **Autenticación 2FA con TOTP** (Time-based One-Time Password)
- ✅ **Códigos de recuperación** de un solo uso
- ✅ **Gestión completa de usuarios** (CRUD)
- ✅ **Estadísticas del sistema** y monitoreo
- ✅ **Pruebas automatizadas** durante el despliegue
- ✅ **Rate limiting** por usuario e IP
- ✅ **Cifrado AES-GCM** para secretos TOTP
- ✅ **Documentación Swagger/OpenAPI**
- ✅ **Health checks** y monitoreo

## 📋 Endpoints Disponibles

### 🔐 Gestión de 2FA
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/2fa/enable` | Habilitar 2FA para un usuario |
| POST | `/api/2fa/verify` | Verificar código TOTP |
| POST | `/api/2fa/verify-recovery/{username}` | Verificar código de recuperación |
| POST | `/api/2fa/rotate/{username}` | Rotar secreto 2FA |
| POST | `/api/2fa/disable/{username}` | Deshabilitar 2FA |

### 👥 Gestión de Usuarios
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/users` | Crear nuevo usuario |
| GET | `/api/users/{username}` | Obtener información del usuario |
| GET | `/api/users/{username}/status` | Verificar estado 2FA del usuario |
| DELETE | `/api/users/{username}` | Eliminar usuario y sus datos |

### 📊 Administración
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/stats` | Estadísticas del sistema |
| GET | `/api/admin/users` | Listar usuarios con 2FA habilitado |

### 🏥 Monitoreo
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check del sistema |
| GET | `/swagger-ui/index.html` | Documentación Swagger |

## 🚀 Despliegue Rápido

### Opción 1: Despliegue con pruebas automatizadas (Recomendado)

```bash
# Linux/Mac
./deploy-with-tests.sh

# Windows PowerShell
.\deploy-with-tests.ps1
```

### Opción 2: Docker Compose tradicional

```bash
# Configurar variables de entorno
export ENCRYPTION_KEY="tu_clave_de_cifrado_segura_32bytes"
export JWT_SECRET="tu_jwt_secret_aqui"

# Desplegar
docker-compose up -d --build

# Ejecutar pruebas manualmente
./test-api.sh
```

## 🧪 Pruebas Automatizadas

### Ejecutar todas las pruebas

```bash
# Linux/Mac
./test-api.sh

# Windows
.\test-api.ps1
```

### Pruebas durante el despliegue

```bash
# Ejecutar pruebas como parte del despliegue
docker-compose --profile testing up --build
```

## 📖 Ejemplos de Uso

### 1. Crear usuario
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"demo"}'
```

### 2. Habilitar 2FA
```bash
curl -X POST http://localhost:8080/api/2fa/enable \
  -H "Content-Type: application/json" \
  -d '{"username":"demo"}'
```

### 3. Verificar código TOTP
```bash
curl -X POST http://localhost:8080/api/2fa/verify \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","code":"123456"}'
```

### 4. Verificar estado del usuario
```bash
curl http://localhost:8080/api/users/demo/status
```

### 5. Obtener estadísticas del sistema
```bash
curl http://localhost:8080/api/admin/stats
```

## 🔧 Configuración

### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `ENCRYPTION_KEY` | Clave AES para cifrar secretos (32 bytes) | `1234567890abcdef` |
| `JWT_SECRET` | Secreto para JWT (opcional) | `change_me` |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos | `http://localhost:3000` |

### Seguridad

- Los secretos TOTP se cifran con AES-GCM
- Los códigos de recuperación se hashean con BCrypt
- Rate limiting: 5 intentos por minuto por usuario+IP
- CORS configurado para desarrollo local

## 🧪 Desarrollo

### Ejecutar pruebas unitarias
```bash
mvn test
```

### Ejecutar pruebas de integración
```bash
mvn test -Dtest=TwoFactorAuthIntegrationTest
```

### Construir proyecto
```bash
mvn clean package
```

## 📊 Monitoreo

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Swagger UI
Abrir en navegador: `http://localhost:8080/swagger-ui/index.html`

## 🔍 Solución de Problemas

### La aplicación no inicia
- Verificar que PostgreSQL esté ejecutándose
- Comprobar variables de entorno `ENCRYPTION_KEY` y `JWT_SECRET`
- Revisar logs con `docker-compose logs app`

### Las pruebas fallan
- Asegurar que la aplicación esté completamente iniciada
- Verificar que no haya conflictos de puerto en el 8080
- Ejecutar pruebas manualmente para más detalles

### Problemas con 2FA
- Verificar sincronización de tiempo del servidor
- Los códigos TOTP tienen validez de 30 segundos
- Los códigos de recuperación son de un solo uso

## 📁 Estructura del Proyecto

```
2FA-autohosted/
├── src/main/java/com/argy/twofactorauth/
│   ├── controller/          # Controladores REST
│   ├── service/            # Lógica de negocio
│   ├── repository/         # Acceso a datos
│   ├── entity/             # Entidades JPA
│   ├── dto/                # Objetos de transferencia
│   └── config/             # Configuración
├── src/test/java/          # Pruebas unitarias e integración
├── src/main/resources/     # Configuración y migraciones
├── test-api.sh             # Script de pruebas (Linux/Mac)
├── test-api.ps1            # Script de pruebas (Windows)
├── deploy-with-tests.sh    # Despliegue con pruebas
├── deploy-with-tests.ps1   # Despliegue con pruebas (Windows)
├── docker-compose.yml      # Configuración Docker
└── Dockerfile             # Imagen Docker
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver archivo LICENSE para más detalles.