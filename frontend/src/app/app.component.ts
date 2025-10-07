import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet, ChildrenOutletContexts } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { EventShareCardComponent } from './shared/event-share-card/event-share-card.component';
import { slideInAnimation } from './animations/route-animations';
import { configService } from './services/config.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, EventShareCardComponent],
  template: `
    <main class="min-h-screen flex flex-col font-sans bg-gray-50 dark:bg-gray-900">
      <header class="bg-gradient-to-r from-indigo-500 to-purple-600 dark:from-indigo-600 dark:to-purple-700 text-white shadow-lg">
        <app-header></app-header>
      </header>
      
      <!-- Demo de tarjeta compartible -->
      <div class="p-4">
        <app-event-share-card
          [shortcode]="demoShortcode"
          [qrBase64]="demoQr"
          [title]="demoTitle"
          description="Comparte este enlace corto y su código"
        ></app-event-share-card>
      </div>

      <section class="flex-1 bg-gray-50 dark:bg-gray-900 overflow-hidden" [@routeAnimations]="getRouteAnimationData()">
        <router-outlet />
      </section>
      
<!-- Footer -->
    <footer class="bg-gray-900 text-white py-12">
      <div class="container mx-auto px-6">
        <div class="grid md:grid-cols-4 gap-8">
          <div>
            <h3 class="text-2xl font-bold mb-4 text-gradient bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              RAFFIFY
            </h3>
            <p class="text-gray-400 mb-4">
              La plataforma líder en sorteos y rifas digitales con transparencia blockchain.
            </p>
            <div class="flex space-x-4">
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/>
                </svg>
              </a>
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M22.46 6c-.77.35-1.6.58-2.46.69.88-.53 1.56-1.37 1.88-2.38-.83.5-1.75.85-2.72 1.05C18.37 4.5 17.26 4 16 4c-2.35 0-4.27 1.92-4.27 4.29 0 .34.04.67.11.98C8.28 9.09 5.11 7.38 3 4.79c-.37.63-.58 1.37-.58 2.15 0 1.49.75 2.81 1.91 3.56-.71 0-1.37-.2-1.95-.5v.03c0 2.08 1.48 3.82 3.44 4.21a4.22 4.22 0 0 1-1.93.07 4.28 4.28 0 0 0 4 2.98 8.521 8.521 0 0 1-5.33 1.84c-.34 0-.68-.02-1.02-.06C3.44 20.29 5.7 21 8.12 21 16 21 20.33 14.46 20.33 8.79c0-.19 0-.37-.01-.56.84-.6 1.56-1.36 2.14-2.23z"/>
                </svg>
              </a>
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12.017 0C5.396 0 .029 5.367.029 11.987c0 5.079 3.158 9.417 7.618 11.174-.105-.949-.199-2.403.041-3.439.219-.937 1.406-5.957 1.406-5.957s-.359-.72-.359-1.781c0-1.663.967-2.911 2.168-2.911 1.024 0 1.518.769 1.518 1.688 0 1.029-.653 2.567-.992 3.992-.285 1.193.6 2.165 1.775 2.165 2.128 0 3.768-2.245 3.768-5.487 0-2.861-2.063-4.869-5.008-4.869-3.41 0-5.409 2.562-5.409 5.199 0 1.033.394 2.143.889 2.741.099.12.112.225.085.345-.09.375-.293 1.199-.334 1.363-.053.225-.172.271-.402.165-1.495-.69-2.433-2.878-2.433-4.646 0-3.776 2.748-7.252 7.92-7.252 4.158 0 7.392 2.967 7.392 6.923 0 4.135-2.607 7.462-6.233 7.462-1.214 0-2.357-.629-2.75-1.378l-.748 2.853c-.271 1.043-1.002 2.35-1.492 3.146C9.57 23.812 10.763 24.009 12.017 24.009c6.624 0 11.99-5.367 11.99-11.988C24.007 5.367 18.641.001 12.017.001z"/>
                </svg>
              </a>
            </div>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Producto</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Características</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Precios</a></li>
              <li><a href="#" class="hover:text-white transition-colors">API</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Integraciones</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Soporte</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Centro de Ayuda</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Documentación</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Contacto</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Estado del Sistema</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Legal</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Términos de Uso</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Política de Privacidad</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Cookies</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Transparencia</a></li>
            </ul>
          </div>
        </div>
        
        <div class="border-t border-gray-800 mt-12 pt-8 text-center text-gray-400">
          <p>&copy; 2025 RAFFIFY. Todos los derechos reservados. Desarrollado con ❤️ para modernizar los sorteos.</p>
        </div>
      </div>
    </footer>
    </main>
  `,
  styles: [],
  animations: [slideInAnimation],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  // Componente principal de la aplicación
  // Utiliza OnPush para mejor rendimiento
  
  constructor(
    private contexts: ChildrenOutletContexts
  ) {
    // Removido initData() - ahora se llama solo en las páginas que lo necesitan
  }
  
  getRouteAnimationData() {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
  }

  // Datos de demostración para la tarjeta compartible
  demoShortcode = 'FKILLoxC';
  demoTitle = 'Demo Evento Compartible';
  demoQr?: string = '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' // Provee base64 válido para ver la imagen QR
}
