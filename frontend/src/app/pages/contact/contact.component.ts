import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="container mx-auto px-6 py-12">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">Contacto</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-4">Escríbenos a <a href="mailto:soporte@raffy.app" class="text-primary underline">soporte@raffy.app</a>.</p>
      <p class="text-gray-600 dark:text-gray-300">Pronto agregaremos un formulario de contacto.</p>
    </section>
  `,
  styles: []
})
export class ContactComponent {}