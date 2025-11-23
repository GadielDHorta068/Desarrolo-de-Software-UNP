---
trigger: model_decision
description: when the request is for the frontend 
---


## 1. Stack Tecnológico
- **Framework**: Angular 20+ (Latest)
- **Lenguaje**: TypeScript (Strict Mode recomendado)
- **Estilos**: Tailwind CSS (v3.4+)
- **Componentes UI**: Preline, Angular Material (selectivo)
- **Empaquetador**: Angular CLI (Build System)

## 2. Arquitectura y Estructura
El proyecto sigue una arquitectura modular basada en características (features) y capas de responsabilidad:

- **`src/app/pages/`**: Componentes de página (Vistas completas). Cada carpeta representa una ruta o sección principal.
- **`src/app/shared/components/`**: Componentes reutilizables (Dumb Components) como botones, inputs, tarjetas, etc.
- **`src/app/services/`**: Lógica de negocio, llamadas HTTP y gestión de estado global.
- **`src/app/models/`**: Interfaces y tipos TypeScript para definir la estructura de datos.
- **`src/app/guards/`**: Protecciones de rutas.
- **`src/app/interceptors/`**: Interceptores HTTP (ej. para tokens de autenticación).

## 3. Convenciones de Código

### Componentes
- **Standalone Components**: Todos los componentes deben ser `standalone: true` (por defecto en Angular 19+).
- **Estructura de Archivos**:
  - `nombre-componente.ts`: Lógica.
  - `nombre-componente.html`: Template.
  - `nombre-componente.css`: Estilos específicos (minimizar uso, preferir Tailwind).
- **Nombres**:
  - Archivos: `kebab-case` (ej. `mp-brick.ts`).
  - Clases: `PascalCase` (ej. `MpBrick`).
  - Selectores: `app-kebab-case` (ej. `app-mp-brick`).

### Estilos (Tailwind CSS)
- **Prioridad**: Usar siempre clases de utilidad de Tailwind antes de escribir CSS personalizado.
- **Colores**: Utilizar los colores semánticos configurados en `tailwind.config.js`:
  - `primary`, `secondary`, `accent`.
- **Modo Oscuro**: Soportado mediante la clase `dark` (`darkMode: 'class'`).
- **Responsive**: Diseñar Mobile-First utilizando prefijos (`sm:`, `md:`, `lg:`).

### Manejo de Estado y Reactividad
- **Signals**: Preferir el uso de Angular Signals para el estado local de los componentes y reactividad síncrona.
- **RxJS**: Utilizar para manejo de eventos asíncronos complejos, `HttpClient`, y `Forms`.
  - Evitar "nested subscriptions". Usar operadores como `switchMap`, `mergeMap`.
  - Desuscribirse siempre (usando `takeUntilDestroyed` o `AsyncPipe`).

### Inyección de Dependencias
- Se permite inyección por **Constructor** (estilo clásico) o función **`inject()`** (estilo moderno). Mantener consistencia dentro de cada archivo.

## 4. Reglas para la IA (Cursor/Windsurf/Antigravity)
- **Generación de Código**:
  - Generar código TypeScript estrictamente tipado. Evitar `any` a menos que sea inevitable (ej. librerías externas sin tipos).
  - Al crear nuevos componentes, asegurar que sean `standalone` e importen sus dependencias.
  - Al estilizar, usar exclusivamente clases de Tailwind a menos que se pida lo contrario.
- **Modificaciones**:
  - Respetar el estilo de código existente en el archivo.
  - No eliminar comentarios importantes (ej. `// TODO`, explicaciones de lógica compleja).

## 5. Comandos Comunes
- `npm start`: Inicia el servidor de desarrollo.
- `npm run build`: Construye para producción.
