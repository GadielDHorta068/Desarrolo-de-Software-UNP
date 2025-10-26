import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { EventsTemp, StatusEvent, EventTypes } from '../../models/events.model';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { AuthService } from '../../services/auth.service';
import { Subject, of, Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-public-events',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DrawCard],
  templateUrl: './public-events.html',
  styleUrl: './public-events.css'
})
export class PublicEvents implements OnInit, AfterViewInit {
  events: EventsTemp[] = [];
  filteredEvents: EventsTemp[] = [];
  visibleEvents: EventsTemp[] = [];
  loading = true;
  error = '';
  public StatusEvent = StatusEvent;
  public EventTypes = EventTypes;
  selectedStatus: 'ALL' | StatusEvent = 'ALL';
  selectedType: 'ALL' | EventTypes = 'ALL';
  userLogged: boolean = false;

  // modal de invitación
  showLoginModal = false;

  // búsqueda
  searchTerm: string = '';
  private searchInput$ = new Subject<string>();

  // lazy loading
  pageSize = 9;
  private currentIndex = 0;
  @ViewChild('sentinel') sentinel!: ElementRef;
  private observer: IntersectionObserver | null = null;

  constructor(
    private eventsService: EventsService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router: Router
  ){}

  ngOnInit(): void {
    this.userLogged = this.authService.isAuthenticated();
    this.setupSearch();
    // disparo inicial para traer por estado seleccionado (ALL por defecto)
    this.searchInput$.next('');
  }

  ngAfterViewInit(): void {
    this.setupInfiniteScroll();
  }

  private setupSearch(): void {
    this.searchInput$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(term => {
          this.loading = true;
          this.error = '';
          if (term && term.length >= 3) {
            return this.eventsService.searchEvents(term).pipe(
              catchError(err => {
                console.error('[PublicEvents] Error en búsqueda:', err);
                this.error = 'No se pudo realizar la búsqueda.';
                return of([]);
              })
            );
          } else {
            return this.getSourceByFilters().pipe(
              catchError(err => {
                console.error('[PublicEvents] Error cargando por filtros:', err);
                this.error = 'No se pudieron cargar los eventos.';
                return of([]);
              })
            );
          }
        })
      )
      .subscribe(results => {
        this.events = results || [];
        // aplicar filtros locales según corresponda
        if (this.searchTerm && this.searchTerm.length >= 3) {
          this.filteredEvents = this.events
            .filter(evt => this.selectedStatus === 'ALL' || evt.statusEvent === this.selectedStatus)
            .filter(evt => this.selectedType === 'ALL' || evt.eventType === this.selectedType);
        } else {
          // si la fuente fue por tipo y también hay estado, aplico estado localmente
          if (this.selectedType !== 'ALL' && this.selectedStatus !== 'ALL') {
            this.filteredEvents = this.events.filter(evt => evt.statusEvent === this.selectedStatus);
          } else {
            this.filteredEvents = this.events;
          }
        }
        this.resetVisible();
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // Decide qué fuente usar cuando NO hay búsqueda activa
  private getSourceByFilters(): Observable<EventsTemp[]> {
    if (this.selectedType !== 'ALL') {
      return this.eventsService.getEventsByType(this.selectedType as EventTypes);
    }
    if (this.selectedStatus !== 'ALL') {
      return this.eventsService.getEventsByStatus(this.selectedStatus as StatusEvent);
    }
    return this.eventsService.getAllEvents();
  }

  private setupInfiniteScroll(): void {
    if (!this.sentinel) return;
    this.observer = new IntersectionObserver(entries => {
      const entry = entries[0];
      if (entry.isIntersecting) {
        this.loadMore();
        this.cdr.detectChanges();
      }
    }, { threshold: 0.1 });
    this.observer.observe(this.sentinel.nativeElement);
  }

  private resetVisible(): void {
    this.visibleEvents = [];
    this.currentIndex = 0;
    this.loadMore();
  }

  private loadMore(): void {
    if (this.currentIndex >= this.filteredEvents.length) return;
    const nextChunk = this.filteredEvents.slice(this.currentIndex, this.currentIndex + this.pageSize);
    this.visibleEvents = this.visibleEvents.concat(nextChunk);
    this.currentIndex += nextChunk.length;
  }

  public onSearchTermChange(value: string): void {
    this.searchTerm = value;
    this.searchInput$.next(value);
  }

  public applyFilter(status: 'ALL' | StatusEvent): void {
    this.selectedStatus = status;

    // Si hay búsqueda activa (>=3), aplico filtros locales
    if (this.searchTerm && this.searchTerm.length >= 3) {
      this.filteredEvents = this.events
        .filter(evt => this.selectedStatus === 'ALL' || evt.statusEvent === this.selectedStatus)
        .filter(evt => this.selectedType === 'ALL' || evt.eventType === this.selectedType);
      this.resetVisible();
      this.cdr.detectChanges();
      return;
    }

    // Sin búsqueda: obtengo por tipo si aplica, o por estado, o todos
    this.loading = true;
    this.error = '';
    this.getSourceByFilters().pipe(
      catchError(err => {
        console.error('[PublicEvents] Error al aplicar filtro:', err);
        this.error = 'No se pudieron cargar los eventos por filtro.';
        this.loading = false;
        return of([]);
      })
    ).subscribe(results => {
      this.events = results || [];
      // si hay tipo y estado, aplicar estado localmente
      if (this.selectedType !== 'ALL' && this.selectedStatus !== 'ALL') {
        this.filteredEvents = this.events.filter(evt => evt.statusEvent === this.selectedStatus);
      } else {
        this.filteredEvents = this.events;
      }
      this.resetVisible();
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  public applyTypeFilter(type: 'ALL' | EventTypes): void {
    this.selectedType = type;

    // Con búsqueda activa: filtros locales
    if (this.searchTerm && this.searchTerm.length >= 3) {
      this.filteredEvents = this.events
        .filter(evt => this.selectedStatus === 'ALL' || evt.statusEvent === this.selectedStatus)
        .filter(evt => this.selectedType === 'ALL' || evt.eventType === this.selectedType);
      this.resetVisible();
      this.cdr.detectChanges();
      return;
    }

    // Sin búsqueda: obtengo por tipo si aplica, o por estado, o todos
    this.loading = true;
    this.error = '';
    this.getSourceByFilters().pipe(
      catchError(err => {
        console.error('[PublicEvents] Error al aplicar filtro por tipo:', err);
        this.error = 'No se pudieron cargar los eventos por tipo.';
        this.loading = false;
        return of([]);
      })
    ).subscribe(results => {
      this.events = results || [];
      // si hay tipo y estado, aplicar estado localmente
      if (this.selectedType !== 'ALL' && this.selectedStatus !== 'ALL') {
        this.filteredEvents = this.events.filter(evt => evt.statusEvent === this.selectedStatus);
      } else {
        this.filteredEvents = this.events;
      }
      this.resetVisible();
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  public clearFilters(): void {
    this.selectedStatus = 'ALL';
    this.selectedType = 'ALL';
    this.searchTerm = '';
    this.searchInput$.next(''); // recargar sin búsqueda y sin filtros
  }

  // navegación y modal para la invitación
  openLoginModal(): void {
    this.showLoginModal = true;
  }

  closeLoginModal(): void {
    this.showLoginModal = false;
  }

  navigateToRegister(): void {
    this.showLoginModal = false;
    this.router.navigate(['/register']);
  }

  navigateToLogin(): void {
    this.showLoginModal = false;
    this.router.navigate(['/login']);
  }

  // método antiguo, ya no se usa directamente
  private loadPublicEvents(): void {
    this.eventsService.getAllEvents().subscribe({
      next: (response) => {
        this.events = response || [];
        this.applyFilter(this.selectedStatus);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('[PublicEvents] Error al recuperar eventos públicos:', err);
        this.error = 'No se pudieron cargar los eventos.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
