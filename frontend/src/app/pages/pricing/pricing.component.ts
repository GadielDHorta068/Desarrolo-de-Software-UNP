import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Precios</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-6">Planes simples para comenzar. Próximamente más opciones.</p>
      <div class="grid md:grid-cols-3 gap-6">
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Gratis</h2>
          <p class="text-gray-600 dark:text-gray-300">Crea sorteos básicos sin costo.</p>
        </div>
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Pro</h2>
          <p class="text-gray-600 dark:text-gray-300">Funcionalidades avanzadas. En desarrollo.</p>
        </div>
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Empresas</h2>
          <p class="text-gray-600 dark:text-gray-300">Escalabilidad y soporte. Próximamente.</p>
        </div>
      </div>
    </section>
  `,
  styles: []
})
export class PricingComponent {}