import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transparency',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Transparencia</h1>
      <p class="text-gray-600 dark:text-gray-300">Compromiso con sorteos verificables y auditables.</p>
    </section>
  `,
  styles: []
})
export class TransparencyComponent {}