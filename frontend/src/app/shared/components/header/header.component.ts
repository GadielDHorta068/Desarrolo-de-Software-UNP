import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  isAuthenticated = false;
  currentUser: any = null;
  isMenuOpen = false;
  isUserMenuOpen = false;

  constructor(
    private authService: AuthService,
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
      } else {
        this.currentUser = null;
      }
      // Forzar detección de cambios
      this.cdr.detectChanges();
    });
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

  closeMenus(): void {
    this.isMenuOpen = false;
    this.isUserMenuOpen = false;
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