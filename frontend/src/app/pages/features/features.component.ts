import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Características</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-6">Descubre las funciones principales de Raffify.</p>
      <div class="grid md:grid-cols-3 gap-6">
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Transparencia</h2>
          <p class="text-gray-600 dark:text-gray-300">Auditorías e historial para cada sorteo.</p>
        </div>
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Participación</h2>
          <p class="text-gray-600 dark:text-gray-300">Mecanismos de inscripción y verificación.</p>
        </div>
        <div class="card">
          <h2 class="text-xl font-semibold mb-2">Integraciones</h2>
          <p class="text-gray-600 dark:text-gray-300">Conecta con servicios externos fácilmente.</p>
        </div>
      </div>
    </section>
  `,
  styles: []
})
export class FeaturesComponent {}