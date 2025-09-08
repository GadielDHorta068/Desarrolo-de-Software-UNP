import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  template: `
    <main class="min-h-screen flex flex-col font-sans bg-gray-50 dark:bg-gray-900">
      <header class="bg-gradient-to-r from-indigo-500 to-purple-600 dark:from-indigo-600 dark:to-purple-700 text-white shadow-lg">
        <app-header></app-header>
      </header>
      
      <section class="flex-1 bg-gray-50 dark:bg-gray-900">
        <router-outlet />
      </section>
      
      <footer class="bg-gray-800 dark:bg-gray-900 text-white text-center py-4 mt-auto border-t border-gray-700 dark:border-gray-600">
        <p class="text-sm text-gray-300 dark:text-gray-400">&copy; Cual es la diferencia entre un chiste y tres pijas ?
          Tu vieja no se banca un chiste</p>
      </footer>
    </main>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  // Componente principal de la aplicación
  // Utiliza OnPush para mejor rendimiento
}
