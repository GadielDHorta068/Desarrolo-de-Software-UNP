# Guía de Despliegue - Raffy

## Configuración de URLs de API

### Desarrollo Local
```bash
# Usa proxy de Angular para redirigir peticiones al backend
docker-compose up
```
- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Las peticiones del frontend se redirigen automáticamente al backend usando proxy

### Producción
```bash
# Usa URLs relativas al servidor donde está hosteado
docker-compose -f docker-compose.prod.yml up
```
- Frontend: `https://raffyfy.argcloud.com.ar`
- Backend: `https://raffyfy.argcloud.com.ar:8080`
- Las peticiones van al mismo servidor pero puerto 8080

## Configuración de Entornos

### Desarrollo
- **Archivo**: `docker/frontend.env`
- **API_URL**: `localhost:8080`
- **Comportamiento**: Usa proxy de Angular para redirigir peticiones

### Producción
- **Archivo**: `docker/frontend-prod.env`
- **API_URL**: `:8080`
- **Comportamiento**: Usa URL relativa al dominio donde está hosteado

## Cómo Funciona

### En Desarrollo:
1. El frontend hace peticiones a `/api/*`, `/auth/*`, etc.
2. Angular proxy redirige estas peticiones a `http://localhost:8080`
3. El backend responde normalmente

### En Producción:
1. El frontend hace peticiones a `:8080/api/*`, `:8080/auth/*`, etc.
2. El navegador resuelve `:8080` como `https://raffyfy.argcloud.com.ar:8080`
3. Las peticiones van al mismo servidor pero puerto 8080

## Ventajas de esta Configuración

✅ **Desarrollo**: Fácil debugging con proxy local
✅ **Producción**: URLs relativas funcionan en cualquier dominio
✅ **Flexibilidad**: Misma aplicación funciona en diferentes servidores
✅ **Seguridad**: No hay URLs hardcodeadas en el código

## Comandos Útiles

```bash
# Desarrollo
docker-compose up

# Producción
docker-compose -f docker-compose.prod.yml up

# Solo backend
docker-compose up db backend

# Solo frontend
docker-compose up frontend
```
