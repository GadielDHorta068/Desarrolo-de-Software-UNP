import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-api',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">API</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-6">Explora la API de Raffy. Documentación técnica disponible en el backend Swagger.</p>
      <a href="/swagger-ui/index.html" class="btn-primary">Abrir Swagger</a>
    </section>
  `,
  styles: []
})
export class ApiComponent {}