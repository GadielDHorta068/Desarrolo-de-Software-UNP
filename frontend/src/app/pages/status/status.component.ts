import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Estado del Sistema</h1>
      <p class="text-gray-600 dark:text-gray-300">Todos los servicios operativos.</p>
      <div class="mt-6 grid md:grid-cols-2 gap-4">
        <div class="card">
          <h2 class="text-lg font-semibold">Backend API</h2>
          <p class="text-green-600 dark:text-green-400">Operativo</p>
        </div>
        <div class="card">
          <h2 class="text-lg font-semibold">Base de Datos</h2>
          <p class="text-green-600 dark:text-green-400">Operativa</p>
        </div>
      </div>
    </section>
  `,
  styles: []
})
export class StatusComponent {}