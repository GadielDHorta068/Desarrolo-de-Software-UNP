## Objetivo

Implementar eventos privados: no se listan públicamente; el organizador los ve en su perfil; acceso sin autenticación sólo mediante enlace/QR único; bloqueo del acceso directo.

## Cambios en Backend

### Modelo y esquema

* Añadir `isPrivate: boolean` en `Events` con default `false` (`backend/src/main/java/com/desarrollo/raffy/model/Events.java`).

* Columna: `event.is_private BOOLEAN NOT NULL DEFAULT FALSE` (Hibernate `ddl-auto=update` ya aplica; no hay Flyway en este módulo).

### DTO y mapeos

* Extender `EventSummaryDTO` con `isPrivate: boolean` (`backend/src/main/java/com/desarrollo/raffy/dto/EventSummaryDTO.java`).

* Asegurar mapeo en `ModelMapperConfig` para incluir `isPrivate` (`backend/src/main/java/com/desarrollo/raffy/config/ModelMapperConfig.java`).

### Queries y endpoints de listado

* Excluir privados en listados y búsquedas públicas:

  * `EventsService.getAllEventSummaries()` → usa `eventsRepository.findAllWithDetails()`; añadir `WHERE e.isPrivate = FALSE` en query.

  * Filtros: `findByStatusEvent`, `findByEventType`, `findByCategoryId`, `findByDateRange`, `findByStartDate`, `findByEndDate`, `findByTitleContainingIgnoreCase`, `findActiveEvents`, `findRecentByType` y “top por participación” → añadir `e.isPrivate = FALSE`.

  * Referencias:

    * Controlador: `EventsController` (`backend/src/main/java/com/desarrollo/raffy/presenter/EventsController.java:560`) `GET /events/all`.

    * Servicio: `EventsService` (`backend/src/main/java/com/desarrollo/raffy/business/services/EventsService.java:355`) `getAllEventSummaries`.

    * Repositorio: `EventsRepository` (métodos listados por el agente de búsqueda).

* Mantener privados visibles para el organizador:

  * `GET /events/creator/{idCreator}` ya usa `eventsRepository.findByCreatorId(...)` → no filtrar por `isPrivate` aquí (`EventsService.getEventSummariesByCreator` en `backend/src/main/java/com/desarrollo/raffy/business/services/EventsService.java:221`).

### Acceso al detalle: invitación obligatoria

* Endpoint de detalle `GET /events/id/{id}`: si `event.isPrivate` y el solicitante no es el creador, exigir `invite` (query param) válido; si falta/incorrecto → `403`.

* Validación de invitación:

  * Inyectar `UrlService` en `EventsController`.

  * Verificar `invite` con `urlService.getUrlByShortcode(invite)` y que esté asociado al evento.

* Asociación URL↔Evento (para robustez y futura “sólo por invitación”):

  * Extender entidad `Url` con `eventId: Long` (columna nullable) para vincular invitaciones a eventos (`backend/src/main/java/com/desarrollo/raffy/model/Url.java`).

  * Extender `UrlService.saveUrlForEvent(eventId, originalUrl)` que setee `eventId`.

  * Nuevo endpoint `POST /api/url/event/{eventId}/save` que retorna `{ url, qr }`.

* Alternativa mínima (si se prefiere menos cambios): validar que el `originalUrl` de la URL corta contiene el `eventId` del detalle; la propuesta de `eventId` en `Url` es más sólida y prepara el cambio futuro.

## Cambios en Frontend

### Modelos y servicios

* Añadir `isPrivate: boolean` en `EventsTemp` (`frontend/src/app/models/events.model.ts`).

* Propagar `isPrivate` desde respuestas del backend.

* `EventsService.getEventById(eventId, invite?)`: opcionalmente aceptar `invite` y enviarlo como query param (`GET /events/id/{id}?invite=...`).

### Listas públicas y perfil

* `PublicEvents` (`frontend/src/app/pages/public-events/public-events.ts`): sin cambios de lógica; backend ya excluye privados.

* `Profile` (`frontend/src/app/pages/profile/profile.ts`): mostrar todos los eventos del organizador, incluyendo privados (vía `getAllByCreator`).

### Vista de detalle y bloqueo

* `ManagementEvent` (`frontend/src/app/pages/management-event/management-event.ts`): al cargar, leer `eventId` y `invite` de `ActivatedRoute`.

  * Si el evento es privado y el usuario no es el creador y no hay `invite`, mostrar “Acceso restringido / Evento privado” y no cargar detalles.

  * Si hay `invite`, llamar `EventsService.getEventById(eventId, invite)`.

* Añadir señales visuales (candado) cuando `isPrivate=true` en tarjetas (`shared/components/draw-card/draw-card.ts`).

### Compartir enlace y QR

* `EventShareCardComponent` (`frontend/src/app/shared/event-share-card/event-share-card.component.ts`):

  * Sólo visible al organizador del evento.

  * Al generar, crear URL corta cuyo `originalUrl` sea el enlace al detalle con `invite` (p.ej. `/event/management/{id}?invite={shortcode}`).

  * Copiar “Compartir enlace” y mostrar/descargar “Generar código QR”.

  * Reutilizar `UrlService.saveUrl(...)` y `convertLinkToQr(...)`. Preparar para cambiar a “sólo por invitación” usando el nuevo endpoint `saveUrlForEvent` cuando se habilite.

## Flujo de acceso

1. Organizador marca “Privado” al crear/editar.
2. Eventos privados no aparecen en listados/búsqueda.
3. El organizador ve sus privados en su perfil.
4. El organizador comparte enlace corto/QR desde `ManagementEvent`.
5. El visitante accede vía `/api/url/redirect/{shortcode}` → redirige a `/event/management/{id}?invite={shortcode}`.
6. Backend valida `invite` en `GET /events/id/{id}`; si válido, retorna datos; si no, `403`.

## Pruebas

### Backend

* Unit tests en `EventsRepository` para asegurar `isPrivate=TRUE` se excluye en todas las consultas públicas.

* Tests de `EventsController`:

  * Detalle público: privado sin `invite` → `403`.

  * Detalle con `invite` válido → `200`.

  * Detalle como creador sin `invite` → `200`.

* Tests de `UrlService` con vinculación `eventId`.

### Frontend

* Verificar que `PublicEvents` no muestre privados.

* `Profile` muestre privados del organizador.

* `ManagementEvent`: sin `invite` y no creador → mensaje de bloqueo; con `invite` → muestra.

* `EventShareCardComponent`: copia enlace y genera QR; sólo visible al creador.

## Consideraciones de seguridad y extensión

* No exponer datos de eventos privados sin `invite` o sin ser creador.

* Asociar URL corta al `eventId` evita reutilización indebida.

* Diseño preparado para evolución a “sólo por invitación” (deshabilitar acceso directo incluso autenticado). ¿Confirmamos esta dirección para implementar?

