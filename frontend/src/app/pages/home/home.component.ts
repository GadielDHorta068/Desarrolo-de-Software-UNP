import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../shared/components/header/header.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  // Componente de página de inicio con landing page completa para RAFFIFY
  
  // Método para manejar el click en "Crear Sorteo Gratis"
  onCreateRaffle(): void {
    // TODO: Implementar navegación a la página de creación de sorteos
    console.log('Navegando a crear sorteo...');
  }
  
  // Método para manejar el click en "Ver Demo"
  onViewDemo(): void {
    // TODO: Implementar modal o navegación a demo
    console.log('Mostrando demo...');
  }
  
  // Método para manejar el click en "Comenzar Gratis"
  onGetStarted(): void {
    // TODO: Implementar navegación a registro
    console.log('Navegando a registro...');
  }
  
  // Método para manejar el click en "Ver Precios"
  onViewPricing(): void {
    // TODO: Implementar navegación a página de precios
    console.log('Navegando a precios...');
  }
  
  // Método para participar en sorteo de ejemplo
  onParticipateExample(): void {
    // TODO: Implementar lógica de participación
    alert('¡Funcionalidad de participación próximamente!');
  }
}

