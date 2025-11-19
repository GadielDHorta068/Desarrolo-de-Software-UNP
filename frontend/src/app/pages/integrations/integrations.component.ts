import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-integrations',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Integraciones</h1>
      <p class="text-gray-600 dark:text-gray-300">Próximamente integraciones con servicios populares de pago y comunicación.</p>
    </section>
  `,
  styles: []
})
export class IntegrationsComponent {}