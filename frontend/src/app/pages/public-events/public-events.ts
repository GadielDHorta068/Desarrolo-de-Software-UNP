import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { EventsTemp, StatusEvent, EventTypes } from '../../models/events.model';
import { Category, CategoryService } from '../../services/category.service';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { AuthService } from '../../services/auth.service';
import { Subject, of, Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule, MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

@Component({
  selector: 'app-public-events',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DrawCard, MatFormFieldModule, MatInputModule, MatDatepickerModule, MatNativeDateModule, MatAutocompleteModule, MatOptionModule],
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
  selectedType: 'ALL' | EventTypes = 'ALL'; // por defecto: todos
  userLogged: boolean = false;

  // filtros progresivos (endpoint /events/active)
  // Se aplica solo si coincide con una categoría existente
  filterCategory: string = '';
  categories: Category[] = [];
  filteredCategories: Category[] = [];
  categoryQuery: string = '';
  selectedCategoryId?: number;
  filterStart?: string; // YYYY-MM-DD
  filterEnd?: string;   // YYYY-MM-DD
  filterWinners?: number | null;

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
    private router: Router,
    private categoryService: CategoryService
  ){}

  ngOnInit(): void {
    this.userLogged = this.authService.isAuthenticated();
    // cargar categorías existentes (endpoint público)
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats || [];
        this.filteredCategories = this.categories;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.warn('[PublicEvents] No se pudieron cargar categorías:', err);
      }
    });
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const lastMonth = new Date(today.getFullYear(), today.getMonth(), 0);
    lastMonth.setHours(0,0,0,0);
    
    this.filterStart = this.formatDate(lastMonth);
    
    this.onFiltersChanged();

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
          // Siempre usar /events/active; aplico búsqueda local sobre los resultados
          return this.getSourceByFilters().pipe(
            catchError(err => {
              console.error('[PublicEvents] Error cargando por filtros:', err);
              this.error = 'No se pudieron cargar los eventos.';
              return of([]);
            })
          );
        })
      )
      .subscribe(results => {
        this.events = results || [];
        // búsqueda local por título/descripcion sobre resultados de /active
        const term = this.searchTerm.trim().toLowerCase();
        const byText = term.length >= 1
          ? this.events.filter(evt => {
              const title = (evt.title || '').toLowerCase();
              const desc = (evt.description || '').toLowerCase();
              return title.includes(term) || desc.includes(term);
            })
          : this.events;
        // el tipo y demás filtros ya se aplican en backend
        this.filteredEvents = byText;
        this.resetVisible();
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // Decide la fuente siempre usando /events/active con estado opcional
  private getSourceByFilters(): Observable<EventsTemp[]> {
    const options = {
      type: this.selectedType !== 'ALL' ? (this.selectedType as EventTypes) : undefined,
      // Enviamos ID de categoría si está seleccionado
      categorie: this.selectedCategoryId ? String(this.selectedCategoryId) : undefined,
      start: this.filterStart || undefined,
      end: this.filterEnd || undefined,
      winnerCount: this.filterWinners ?? undefined,
      status: this.selectedStatus
    };
    return this.eventsService.getActiveEvents(options);
  }

  private applyLocalFilters(source: EventsTemp[], opts: {
    type?: EventTypes;
    categorieId?: number;
    start?: string;
    end?: string;
    winners?: number | null | undefined;
  }): EventsTemp[] {
    const parseDate = (iso?: string): Date | null => {
      if (!iso) return null;
      const [y, m, d] = iso.split('-').map(n => parseInt(n, 10));
      if (!y || !m || !d) return null;
      return new Date(y, m - 1, d);
    };
    const toDate = (arr: number[]): Date => {
      // asumo [YYYY, MM, DD, ...]
      const y = arr?.[0];
      const m = (arr?.[1] || 1) - 1;
      const d = arr?.[2] || 1;
      return new Date(y, m, d);
    };

    const startLimit = parseDate(opts.start);
    const endLimit = parseDate(opts.end);

    return source
      .filter(evt => !opts.type || evt.eventType === opts.type)
      .filter(evt => !opts.categorieId || evt.categoryId === opts.categorieId)
      .filter(evt => {
        if (!startLimit) return true;
        const evtStart = toDate(evt.startDate);
        return evtStart.getTime() >= startLimit.getTime();
      })
      .filter(evt => {
        if (!endLimit) return true;
        const evtEnd = toDate(evt.endDate);
        return evtEnd.getTime() <= endLimit.getTime();
      })
      .filter(evt => opts.winners == null || opts.winners === undefined || evt.winnersCount === opts.winners);
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
    this.onFiltersChanged();
  }

  public applyTypeFilter(type: 'ALL' | EventTypes): void {
    this.selectedType = type;
    this.onFiltersChanged();
  }

  // llamado común cuando cambian los filtros progresivos
  public onFiltersChanged(): void {
    // Consultar fuente según estado y aplicar filtros locales si corresponde
    this.loading = true;
    this.error = '';
    this.getSourceByFilters().pipe(
      catchError(err => {
        console.error('[PublicEvents] Error al aplicar filtros:', err);
        this.error = 'No se pudieron cargar los eventos por filtros.';
        this.loading = false;
        return of([]);
      })
    ).subscribe(results => {
      this.events = results || [];
      // Los filtros progresivos quedan aplicados en backend; solo búsqueda local
      const working = this.events;
      // aplicar búsqueda local sobre los resultados (>= 3 caracteres)
      const term = this.searchTerm.trim().toLowerCase();
      this.filteredEvents = term.length >= 3
        ? working.filter(evt => (evt.title || '').toLowerCase().includes(term))
        : working;
      this.resetVisible();
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  public clearFilters(): void {
    this.selectedStatus = 'ALL';
    this.selectedType = 'ALL';
    this.searchTerm = '';
    this.filterCategory = '';
    this.categoryQuery = '';
    this.selectedCategoryId = undefined;
    this.filterStart = undefined;
    this.filterEnd = undefined;
    this.filterWinners = null;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const lastMonth = new Date(today.getFullYear(), today.getMonth(), 0);
    lastMonth.setHours(0,0,0,0);
    
    this.filterStart = this.formatDate(lastMonth);
    // recargar sin búsqueda y sin filtros
    this.onFiltersChanged();
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

  // categoría: actualizar consulta y selección segura
  public onCategoryInputChange(value: string): void {
    this.categoryQuery = value;
    const q = value.trim().toLowerCase();
    // filtrar sugerencias
    this.filteredCategories = this.categories.filter(c => c.name.toLowerCase().includes(q));
    // si hay coincidencia exacta, seleccionar
    const match = this.categories.find(c => c.name.toLowerCase() === q);
    if (match) {
      this.selectedCategoryId = match.id;
      this.filterCategory = match.name;
    } else {
      this.selectedCategoryId = undefined;
    }
    this.onFiltersChanged();
  }

  public onCategorySelected(name: string): void {
    const match = this.categories.find(c => c.name.toLowerCase() === name.trim().toLowerCase());
    this.selectedCategoryId = match?.id;
    this.filterCategory = match?.name || '';
    this.categoryQuery = match?.name || '';
    this.onFiltersChanged();
  }

  // Handlers de datepicker y utilidades
  public onStartDateChange(event: MatDatepickerInputEvent<Date>): void {
    const date = event.value || null;
    this.filterStart = date ? this.formatDate(date) : undefined;
    this.onFiltersChanged();
  }

  public onEndDateChange(event: MatDatepickerInputEvent<Date>): void {
    const date = event.value || null;
    this.filterEnd = date ? this.formatDate(date) : undefined;
    this.onFiltersChanged();
  }

  public toDateObj(iso?: string): Date | null {
    if (!iso) return null;
    const [year, month, day] = iso.split('-').map(Number);
    if (!year || !month || !day) return null;
    const date = new Date(year, month - 1, day);
    date.setHours(0, 0, 0, 0);
    return date;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
