# Integración Evolution API (WhatsApp)

Este proyecto integra un contenedor de Evolution API y expone endpoints en el backend (Spring Boot) para crear/conectar instancias y enviar mensajes de texto vía WhatsApp.

## Requisitos
- Docker Desktop 4.x
- Puertos libres: `8080` (backend), `8081` (Evolution API), `4200` (frontend)

## Variables de entorno
- Defina la API key una sola vez y reutilícela en backend y Evolution:
  - `EVOLUTION_API_KEY` (backend y Evolution API)
  - Opcional: `EVOLUTION_INSTANCE` (instancia por defecto)

Puede establecer estas variables en `docker/backend.env` o en su entorno de sistema. El `docker-compose.yml` ya inyecta `EVOLUTION_API_URL` y `EVOLUTION_API_KEY` al backend y configura `AUTHENTICATION_API_KEY` para Evolution API.

## Levantar el stack

1. Construir y levantar servicios:
   - `docker compose up -d --build`
2. Verificar servicios:
   - Backend: `http://localhost:8080`
   - Evolution API (Swagger/Manager): `http://localhost:8081/`

> Nota: El backend se comunica con Evolution dentro de la red Docker usando `http://evolution-api:8080`.

## Endpoints Backend
Base path: `/api/evolution`

Los endpoints requieren autenticación según la configuración de `SecurityConfig`. Use su flujo de login del proyecto para obtener un JWT y adjuntarlo en `Authorization: Bearer <token>`.

- Crear instancia (WhatsApp Web/Baileys):
  - POST `/api/evolution/instances`
  - Body JSON:
    ```json
    {
      "instanceName": "raffy",
      "qrcode": true,
      "token": "optional-token",
      "number": "optional-number",
      "integration": "WHATSAPP-BAILEYS"
    }
    ```

- Conectar instancia (generar QR o usar número):
  - GET `/api/evolution/instances/{instance}/connect?number=5511999999999`

- Enviar texto:
  - POST `/api/evolution/messages/text`
  - Body JSON:
    ```json
    {
      "instance": "raffy",
      "number": "5511999999999",
      "text": "Hola desde Evolution API",
      "delay": 0
    }
    ```

## Notas
- La API de Evolution valida la cabecera `apikey` que el backend añade automáticamente con `EVOLUTION_API_KEY`.
- El contenedor Evolution guarda estado en volúmenes `evolution_store` y `evolution_instances` definidos en `docker-compose.yml`.
- Si desea probar directamente Evolution API, use `http://localhost:8081` con la cabecera `apikey: <EVOLUTION_API_KEY>`.

## Problemas comunes
- 401 desde Evolution: asegure que `EVOLUTION_API_KEY` del backend coincide con `AUTHENTICATION_API_KEY` del contenedor Evolution.
- Conexión rechazada: confirme que `evolution-api` está `healthy` y accesible desde backend dentro de la red Docker.
- Autenticación del backend: obtenga JWT mediante los endpoints `/auth/**` ya existentes en el proyecto.
