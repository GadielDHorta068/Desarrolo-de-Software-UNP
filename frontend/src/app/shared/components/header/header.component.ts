import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ChatService } from '../../../services/chat.service';
import { interval, Subscription, switchMap, startWith } from 'rxjs';
import { UnreadChatSummary } from '../../../models/message.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
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

  private unreadSub?: Subscription;
  private unreadPeersSub?: Subscription;

  constructor(
    private authService: AuthService,
    private chatService: ChatService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Inicializar datos del usuario si están disponibles
    this.authService.initializeUserData();
    
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
  }

  ngOnDestroy(): void {
    this.stopUnreadPolling();
    this.unreadPeersSub?.unsubscribe();
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

  navigateToProfile(): void {
    this.router.navigate(['/profile']);
    this.closeMenus();
  }

  navigateToSettings(): void {
    this.router.navigate(['/settings']);
    this.closeMenus();
  }

  navigateToHome(): void {
    this.router.navigate(['/']);
    this.closeMenus();
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
    this.closeMenus();
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
    this.closeMenus();
  }

  public navigateToAllDraws(): void {
    this.router.navigate(['/draws/all']);
    this.closeMenus();
  }
}