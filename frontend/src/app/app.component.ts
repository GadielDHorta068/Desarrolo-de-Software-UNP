import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <main class="app-container">
      <header class="app-header">
        <h1>Raffy - Sistema de Gestión</h1>
      </header>
      
      <section class="app-content">
        <router-outlet />
      </section>
      
      <footer class="app-footer">
        <p>&copy; 2024 Raffy. Todos los derechos reservados.</p>
      </footer>
    </main>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    
    .app-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 1rem 2rem;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .app-header h1 {
      margin: 0;
      font-size: 1.8rem;
      font-weight: 300;
    }
    
    .app-content {
      flex: 1;
      padding: 2rem;
      background-color: #f8f9fa;
    }
    
    .app-footer {
      background-color: #343a40;
      color: white;
      text-align: center;
      padding: 1rem;
      margin-top: auto;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  // Componente principal de la aplicación
  // Utiliza OnPush para mejor rendimiento
}
