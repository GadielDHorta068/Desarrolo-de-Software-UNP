import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../shared/components/header/header.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  template: `
  <h1>
    Aca cnstruir un inicio fachero jijo
  </h1>
  `
})
export class HomeComponent {
  // Componente de página de inicio con header incluido
}

