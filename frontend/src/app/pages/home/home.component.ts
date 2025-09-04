import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Contenedor principal con Tailwind CSS -->
    <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        
        <!-- Sección de rafy -->
        <div class="text-center mb-16">
          <h1 class="text-5xl md:text-6xl font-bold text-gradient mb-4">
            ¡Bienvenido a rafiffy!
          </h1>
          <p class="text-xl text-gray-600 max-w-2xl mx-auto">
            Sistema de Sorteos Inteligente y Seguro
          </p>
        </div>
        
        <!-- Grid de características -->
        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
          
          <!-- Tarjeta de Reportes -->
          <div class="card group hover:scale-105 transition-all duration-300">
            <div class="text-6xl mb-4">📊</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Reportes Avanzados</h3>
            <p class="text-gray-600 leading-relaxed">
              Obtén insights valiosos sobre el rendimiento de tu empresa con análisis detallados
            </p>
          </div>
          
          <!-- Tarjeta de Seguridad -->
          <div class="card group hover:scale-105 transition-all duration-300">
            <div class="text-6xl mb-4">🔒</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Seguridad Garantizada</h3>
            <p class="text-gray-600 leading-relaxed">
              Tus datos están protegidos con las mejores prácticas de seguridad y encriptación
            </p>
          </div>
          
          <!-- Tarjeta de Facilidad de Uso -->
          <div class="card group hover:scale-105 transition-all duration-300">
            <div class="text-6xl mb-4">💡</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Fácil de Usar</h3>
            <p class="text-gray-600 leading-relaxed">
              Interfaz intuitiva diseñada para maximizar tu productividad y eficiencia
            </p>
          </div>
        </div>
        
        <!-- Sección de llamada a la acción -->
        <div class="text-center space-y-4 sm:space-y-0 sm:space-x-4 sm:flex sm:justify-center">
          <button class="btn-primary text-lg px-8 py-3">
            Comenzar Ahora
          </button>
          <button class="btn-secondary text-lg px-8 py-3">
            Saber Más
          </button>
        </div>
        
        <!-- Información adicional -->
        <div class="mt-16 text-center">
          <p class="text-gray-500 text-sm">
            Desarrollado con Angular 18 y Tailwind CSS
          </p>
        </div>
        
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {
  // Componente de página de inicio usando Tailwind CSS
  // Diseño moderno y responsive con clases utilitarias de Tailwind
}

