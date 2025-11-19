# Componente `app-loading-indicator`

Indicador de carga reutilizable para mostrar estados de espera en operaciones breves (por ejemplo, habilitar/rotar/verificar 2FA, guardar perfil, cambiar contraseña). Standalone, sin dependencias nuevas, basado en utilidades Tailwind.

## Instalación

- Es standalone; no requiere módulo global.
- Importa el componente en el `imports` del componente Angular donde lo usarás:

```ts
import { LoadingIndicator } from '../../shared/components/loading-indicator/loading-indicator'

@Component({
  standalone: true,
  imports: [LoadingIndicator]
})
export class MiComponente {}
```

- Selector: `app-loading-indicator`.

## API

- `active: boolean` — controla visibilidad. `false` por defecto.
- `message?: string` — texto opcional junto al spinner.
- `type: 'inline' | 'overlay'` — estilo visual. `inline` por defecto.
- `size: 'sm' | 'md' | 'lg'` — tamaño del spinner. `md` por defecto.

## Modos de uso

### Inline

- Para botones, etiquetas y contenido compacto.
- Muestra spinner y mensaje opcional en línea.

```html
<button type="submit" [disabled]="isLoading" class="...">
  <app-loading-indicator
    [active]="isLoading"
    [type]="'inline'"
    [message]="'Guardando...'"
    [size]="'sm'">
  </app-loading-indicator>
  <span class="ml-2">{{ isLoading ? 'Guardando...' : 'Guardar Cambios' }}</span>
</button>
```

### Overlay

- Cubre el contenedor con una capa semitransparente y blur para bloquear interacción.
- El contenedor padre debe tener `class="relative"` para posicionamiento correcto.

```html
<div class="relative bg-white rounded-lg p-6">
  <app-loading-indicator
    [active]="is2FALoading"
    [type]="'overlay'"
    [message]="'Procesando...'"
    [size]="'md'">
  </app-loading-indicator>

  <!-- Contenido de la tarjeta -->
  <h4 class="text-sm font-medium">Código QR</h4>
  <!-- ... -->
</div>
```

## Accesibilidad

- Usa `role="status"` y `aria-live="polite"` para anunciar cambios a lectores de pantalla.
- Proporciona `message` significativo (p. ej., “Guardando...”, “Verificando...”, “Procesando...” ).
- Compatible con dark mode gracias a clases Tailwind.

## Buenas prácticas

- Activa `active` al iniciar la operación (antes de la llamada al servicio) y desactívalo en `next`/`error` del `subscribe`.
- En overlay, asegúrate de que el contenedor tenga `class="relative"` y una altura/caja adecuada para que la capa cubra el área visible.
- Deshabilita controles mientras `active` sea `true` para evitar acciones repetidas.
- Mantén mensajes cortos y consistentes entre pantallas.
- Elige `size` según el contexto: `sm` para botones, `md` para tarjetas, `lg` para paneles grandes.

## Limitaciones

- El overlay usa `absolute inset-0`; si el contenedor no es `relative` o no tiene altura, puede no verse correctamente.
- No incluye skeletons de contenido; si los necesitas, combínalo con placeholders propios.
- No gestiona estado global; se espera que el componente padre controle `active`.

## Referencias

- Componente fuente:
  - `frontend/src/app/shared/components/loading-indicator/loading-indicator.ts`
  - `frontend/src/app/shared/components/loading-indicator/loading-indicator.html`
  - `frontend/src/app/shared/components/loading-indicator/loading-indicator.css`
- Ejemplo integrado (2FA en Settings):
  - `frontend/src/app/pages/settings/settings.component.html:659-661`