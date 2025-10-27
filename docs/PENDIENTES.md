# Pendientes

- [ ] Hacer pública solo la URL de obtener ganadores:
  - `GET /audit/obtain/event/{eventId}/winners`
  - Estado: Parcial. Se agregó `permitAll` en `SecurityConfig`.
  - Siguiente: Verificar/crear el mapping del controlador para esta ruta (puede reutilizar la lógica de `GET /events/winners/event/{eventId}`).