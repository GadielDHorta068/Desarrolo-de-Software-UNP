import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="home-container">
      <div class="welcome-section">
        <h2>¡Bienvenido a Raffy!</h2>
        <p class="subtitle">Sistema de Sorteos</p>
        
        <div class="features-grid">
          
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>Reportes en Tiempo Real</h3>
            <p>Obtén insights valiosos sobre el rendimiento de tu empresa</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>Seguridad Garantizada</h3>
            <p>Tus datos están protegidos con las mejores prácticas de seguridad</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">💡</div>
            <h3>Fácil de Usar</h3>
            <p>Interfaz intuitiva diseñada para maximizar tu productividad</p>
          </div>
        </div>
        
        <div class="cta-section">
          <button class="cta-button primary">Comenzar</button>
          <button class="cta-button secondary">Saber Más</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .home-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }
    
    .welcome-section {
      text-align: center;
    }
    
    .welcome-section h2 {
      font-size: 3rem;
      color: #2c3e50;
      margin-bottom: 0.5rem;
      font-weight: 300;
    }
    
    .subtitle {
      font-size: 1.2rem;
      color: #7f8c8d;
      margin-bottom: 3rem;
    }
    
    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 2rem;
      margin: 3rem 0;
    }
    
    .feature-card {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    
    .feature-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }
    
    .feature-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
    }
    
    .feature-card h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-weight: 600;
    }
    
    .feature-card p {
      color: #7f8c8d;
      line-height: 1.6;
    }
    
    .cta-section {
      margin-top: 3rem;
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }
    
    .cta-button {
      padding: 0.75rem 2rem;
      border: none;
      border-radius: 25px;
      font-size: 1rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s ease;
    }
    
    .cta-button.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
    
    .cta-button.primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    }
    
    .cta-button.secondary {
      background: transparent;
      color: #667eea;
      border: 2px solid #667eea;
    }
    
    .cta-button.secondary:hover {
      background: #667eea;
      color: white;
    }
    
    @media (max-width: 768px) {
      .welcome-section h2 {
        font-size: 2rem;
      }
      
      .features-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
      }
      
      .cta-section {
        flex-direction: column;
        align-items: center;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.Default
})
export class HomeComponent {
  // Componente de página de inicio
  // Muestra información general sobre la aplicación
}
