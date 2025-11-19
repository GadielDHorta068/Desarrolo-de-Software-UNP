import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { EventShareCardComponent } from '../../shared/event-share-card/event-share-card.component';
import { AuthService, UserResponse } from '../../services/auth.service';
import { EventsService } from '../../services/events.service';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, EventShareCardComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  // Componente de página de inicio con landing page completa para RAFFIFY
  
  currentUser: UserResponse | null = null;
  isAuthenticated = false;
  authSubscription: Subscription = new Subscription();
  showLoginModal = false;

  featuredEvents: EventsTemp[] = [];
  selectedType: EventTypes = EventTypes.GIVEAWAY;
  eventTypes: EventTypes[] = [EventTypes.GIVEAWAY, EventTypes.RAFFLES];
  loadingFeatured = false;
  currentIndex = 0;
  autoplayMs = 4000;
  autoplayTimer: any;

  // Datos de demostración para tarjeta compartible
  demoShortcode = 'FKILLoxC';
  demoTitle = 'Invitación al evento';
  demoDescription = 'Comparte el link corto y su código';
  demoQr?: string = undefined; // Provee base64 válido para ver el QR
  demoOriginalUrl?: string = undefined;

  constructor(
    private authService: AuthService,
    private router: Router,
    private eventsService: EventsService,
    private cdr: ChangeDetectorRef,
    private adminEventService: AdminEventService
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
    });
    this.loadFeaturedEvents(this.selectedType);
  }


  ngOnDestroy(): void {
    this.authSubscription.unsubscribe();
    this.stopAutoplay();
  }

  loadFeaturedEvents(type?: EventTypes): void {
    this.loadingFeatured = true;
    this.eventsService.getFeaturedEvents(type).subscribe({
      next: (events) => {
        this.featuredEvents = events || [];
        this.loadingFeatured = false;
        this.currentIndex = 0;
        this.cdr.markForCheck();
        this.restartAutoplay();
      },
      error: () => {
        this.featuredEvents = [];
        this.loadingFeatured = false;
        this.currentIndex = 0;
        this.cdr.markForCheck();
        this.stopAutoplay();
      }
    });
  }

  onChangeType(type: EventTypes): void {
    this.selectedType = type;
    this.loadFeaturedEvents(type);
    this.cdr.markForCheck();
  }

  prevSlide(): void {
    if (this.featuredEvents.length === 0) return;
    this.currentIndex = (this.currentIndex - 1 + this.featuredEvents.length) % this.featuredEvents.length;
    this.cdr.markForCheck();
  }

  nextSlide(): void {
    if (this.featuredEvents.length === 0) return;
    this.currentIndex = (this.currentIndex + 1) % this.featuredEvents.length;
    this.cdr.markForCheck();
  }

  setSlide(i: number): void {
    if (i < 0 || i >= this.featuredEvents.length) return;
    this.currentIndex = i;
    this.cdr.markForCheck();
  }

  restartAutoplay(): void {
    this.stopAutoplay();
    if (this.featuredEvents.length > 1) {
      this.autoplayTimer = setInterval(() => this.nextSlide(), this.autoplayMs);
    }
  }

  stopAutoplay(): void {
    if (this.autoplayTimer) {
      clearInterval(this.autoplayTimer);
      this.autoplayTimer = undefined;
    }
  }

  formatDate(date?: number[]): string {
    if (!date || date.length < 3) return '';
    const [y, m, d] = date;
    const mm = String(m).padStart(2, '0');
    const dd = String(d).padStart(2, '0');
    return `${dd}/${mm}/${y}`;
  }

  getCreatorDisplay(ev: EventsTemp): string {
    const c: any = ev.creator as any;
    if (!c) return '';
    return c.nickname || `${c.name || ''} ${c.surname || ''}`.trim();
  }

  statusClass(s: StatusEvent): string {
    if (s === StatusEvent.ACTIVE) return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    if (s === StatusEvent.OPEN) return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
    if (s === StatusEvent.CLOSED) return 'bg-gray-200 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
    if (s === StatusEvent.FINISHED) return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
    if (s === StatusEvent.BLOCKED) return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
  }

  getStatusLabel(s: StatusEvent): string {
    if (s === StatusEvent.ACTIVE) return 'Activo';
    if (s === StatusEvent.OPEN) return 'Abierto';
    if (s === StatusEvent.CLOSED) return 'Cerrado';
    if (s === StatusEvent.FINISHED) return 'Finalizado';
    if (s === StatusEvent.BLOCKED) return 'Bloqueado';
    return String(s);
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
  
  // Método para participar en sorteo de ejemplo
  onParticipate(ev: EventsTemp): void {
    if (!ev || !ev.id) return;
    this.adminEventService.setSelectedEvent(ev);
    this.router.navigate([`/event/management/${ev.id}`]);
  }
}

