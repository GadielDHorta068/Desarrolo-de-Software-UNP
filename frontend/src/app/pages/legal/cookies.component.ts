import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cookies',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Política de Cookies</h1>
      <p class="text-gray-600 dark:text-gray-300">Uso de cookies y preferencias. Documento preliminar.</p>
    </section>
  `,
  styles: []
})
export class CookiesComponent {}