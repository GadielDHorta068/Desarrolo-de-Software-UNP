import { Component, OnInit, ChangeDetectorRef, OnDestroy, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ChatService } from '../../../services/chat.service';
import { interval, Subscription, switchMap, startWith, Subject, debounceTime, distinctUntilChanged, of, forkJoin, catchError, finalize } from 'rxjs';
import { UnreadChatSummary } from '../../../models/message.model';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../../services/events.service';
import { EventsTemp } from '../../../models/events.model';
import { UserResponse } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  isAuthenticated = false;
  currentUser: any = null;
  isMenuOpen = false;
  isUserMenuOpen = false;
  unreadCount = 0;

  // Dropdown de chats no leídos
  isUnreadOpen = false;
  unreadPeers: UnreadChatSummary[] = [];

  // Tema
  isDarkMode = false;

  private unreadSub?: Subscription;
  private unreadPeersSub?: Subscription;
  private searchSub?: Subscription;

  // Búsqueda en header
  isSearchOpen = false;
  searchTerm = '';
  searching = false;
  searchUsers: UserResponse[] = [];
  searchEvents: EventsTemp[] = [];
  private searchInput$ = new Subject<string>();
  @ViewChild('searchInputRef') searchInputRef?: ElementRef<HTMLInputElement>;

  constructor(
    private authService: AuthService,
    private chatService: ChatService,
    private eventsService: EventsService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private elRef: ElementRef<HTMLElement>
  ) {}

  ngOnInit(): void {
    // Inicializar datos del usuario si están disponibles
    this.authService.initializeUserData();

    // Tema
    this.initTheme();
    
    // Suscribirse al estado de autenticación
    this.authService.isAuthenticated$.subscribe(isAuth => {
      this.isAuthenticated = isAuth;
      if (isAuth) {
        this.loadCurrentUser();
        this.startUnreadPolling();
        this.loadUnreadPeers();
      } else {
        this.currentUser = null;
        this.stopUnreadPolling();
        this.unreadCount = 0;
        this.unreadPeers = [];
      }
      // Forzar detección de cambios
      this.cdr.detectChanges();
    });

    // Configurar búsqueda con debounce y consulta paralela a eventos + perfiles
    this.searchSub = this.searchInput$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(term => {
          const q = (term || '').trim();
          if (!q) {
            this.searchUsers = [];
            this.searchEvents = [];
            this.searching = false;
            this.cdr.detectChanges();
            return of({ users: [], events: [] });
          }
          this.searching = true;
          return forkJoin({
            users: this.authService.searchUsers(q).pipe(catchError(() => of([]))),
            events: this.eventsService.searchEvents(q).pipe(catchError(() => of([])))
          }).pipe(finalize(() => {
            this.searching = false;
          }));
        })
      )
      .subscribe(({ users, events }) => {
        this.searchUsers = users || [];
        const q = (this.searchTerm || '').trim().toLowerCase();
        const base = events || [];
        this.searchEvents = base.filter(evt => {
          const t = (evt.title || '').toLowerCase();
          const d = (evt.description || '').toLowerCase();
          return t.includes(q) || d.includes(q);
        });
        this.cdr.detectChanges();
      });
  }

  ngOnDestroy(): void {
    this.stopUnreadPolling();
    this.unreadPeersSub?.unsubscribe();
    this.searchSub?.unsubscribe();
  }

  // Cerrar menús al hacer clic fuera
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (!target) return;
    if (!this.elRef.nativeElement.contains(target)) {
      this.closeMenus();
      this.closeSearch();
      this.cdr.detectChanges();
    }
  }

  // Cerrar con tecla Escape
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMenus();
  }

  private startUnreadPolling(): void {
    this.stopUnreadPolling();
    // Polling ligero cada 15s, arrancando inmediato
    this.unreadSub = interval(15000).pipe(
      startWith(0),
      switchMap(() => this.chatService.getUnreadCount())
    ).subscribe({
      next: count => { this.unreadCount = count; this.cdr.detectChanges(); },
      error: () => { /* ignorar errores para no romper el header */ }
    });
  }

  private loadUnreadPeers(): void {
    this.unreadPeersSub?.unsubscribe();
    this.unreadPeersSub = this.chatService.getUnreadPeers().subscribe({
      next: peers => { this.unreadPeers = peers; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  private stopUnreadPolling(): void {
    this.unreadSub?.unsubscribe();
    this.unreadSub = undefined;
  }

  private loadCurrentUser(): void {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
      },
      error: (error) => {
        console.error('Error loading user:', error);
      }
    });
  }

  get profileImageSrc(): string {
    if (this.currentUser?.imagen) {
      // Si la imagen ya tiene el prefijo data:, la devolvemos tal como está
      if (this.currentUser.imagen.startsWith('data:')) {
        return this.currentUser.imagen;
      }
      // Si no tiene el prefijo, lo agregamos
      return `data:image/jpeg;base64,${this.currentUser.imagen}`;
    }
    // Imagen por defecto si no hay imagen
    return 'assets/default-profile.jpg';
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  toggleUnread(): void {
    this.isUnreadOpen = !this.isUnreadOpen;
    if (this.isUnreadOpen) {
      this.loadUnreadPeers();
    }
  }

  closeMenus(): void {
    this.isMenuOpen = false;
    this.isUserMenuOpen = false;
    this.isUnreadOpen = false;
  }

  // ---- Búsqueda ----
  toggleSearch(): void {
    this.isSearchOpen = !this.isSearchOpen;
    if (this.isSearchOpen) {
      setTimeout(() => this.searchInputRef?.nativeElement?.focus(), 0);
    } else {
      this.closeSearch();
    }
  }

  onSearchInput(): void {
    this.searchInput$.next(this.searchTerm);
  }

  closeSearch(): void {
    this.isSearchOpen = false;
    this.searchTerm = '';
    this.searchUsers = [];
    this.searchEvents = [];
  }

  goToProfile(nickname: string): void {
    if (!nickname) return;
    this.router.navigate(['/profile', nickname]);
    this.closeSearch();
    this.closeMenus();
  }

  goToEvent(eventId: number): void {
    if (!eventId) return;
    this.router.navigate(['/event/management', eventId]);
    this.closeSearch();
    this.closeMenus();
  }

  openChat(peerId: number): void {
    this.closeMenus();
    this.router.navigate([`/chat/${peerId}`]);
  }

  logout(): void {
    this.closeMenus();
    this.authService.logout().subscribe({
      next: () => {
        // El AuthService ya maneja la redirección
      },
      error: () => {
        // El AuthService ya maneja la redirección incluso en caso de error
      }
    });
  }

  // Navegaciones directas ya no necesarias para desktop; se mantienen por compatibilidad móvil
  navigateToProfile(): void { this.router.navigate(['/profile']); this.closeMenus(); }
  navigateToSettings(): void { this.router.navigate(['/settings']); this.closeMenus(); }
  navigateToHome(): void { this.router.navigate(['/']); this.closeMenus(); }
  navigateToLogin(): void { this.router.navigate(['/login']); this.closeMenus(); }
  navigateToRegister(): void { this.router.navigate(['/register']); this.closeMenus(); }
  public navigateToAllDraws(): void { this.router.navigate(['/draws/all']); this.closeMenus(); }

  // ------ Tema oscuro ------
  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  private initTheme(): void {
    const stored = localStorage.getItem('theme');
    if (stored === 'dark' || stored === 'light') {
      this.isDarkMode = stored === 'dark';
    } else {
      // fallback al media query del sistema
      this.isDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    this.applyTheme();
  }

  private applyTheme(): void {
    const root = document.documentElement; // <html>
    root.classList.toggle('dark', this.isDarkMode);
  }
}