import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { EventShareCardComponent } from '../../shared/event-share-card/event-share-card.component';
import { FeaturedEventsComponent } from '../../shared/components/featured-events/featured-events.component';
import { AuthService, UserResponse } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, EventShareCardComponent, FeaturedEventsComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  // Componente de página de inicio con landing page completa para RAFFIFY

  currentUser: UserResponse | null = null;
  isAuthenticated = false;
  authSubscription: Subscription = new Subscription();
  showLoginModal = false;

  // Datos de demostración para tarjeta compartible
  demoShortcode = 'FKILLoxC';
  demoTitle = 'Invitación al evento';
  demoDescription = 'Comparte el link corto y su código';
  demoQr?: string = undefined; // Provee base64 válido para ver el QR
  demoOriginalUrl?: string = undefined;

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
    });
  }


  ngOnDestroy(): void {
    this.authSubscription.unsubscribe();
  }

  // Método para manejar el click en "Crear Sorteo Gratis"
  onCreateRaffle(): void {
    if (this.isAuthenticated) {
      this.router.navigate(['/event/new']);
    } else {
      this.showLoginModal = true;
    }
  }

  // Método para cerrar el modal de login
  closeLoginModal(): void {
    this.showLoginModal = false;
  }

  // Método para navegar al registro
  navigateToRegister(): void {
    this.showLoginModal = false;
    this.router.navigate(['/register']);
  }

  // Método para navegar al login
  navigateToLogin(): void {
    this.showLoginModal = false;
    this.router.navigate(['/login']);
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
}

