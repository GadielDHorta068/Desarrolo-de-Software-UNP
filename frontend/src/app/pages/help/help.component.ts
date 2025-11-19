import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-help',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Centro de Ayuda</h1>
      <p class="text-gray-600 dark:text-gray-300">Encuentra respuestas a preguntas frecuentes.</p>
      <ul class="list-disc pl-6 text-gray-700 dark:text-gray-300 mt-4">
        <li>¿Cómo crear mi primer sorteo?</li>
        <li>¿Cómo invitar participantes?</li>
        <li>¿Cómo auditar un evento?</li>
      </ul>
    </section>
  `,
  styles: []
})
export class HelpComponent {}