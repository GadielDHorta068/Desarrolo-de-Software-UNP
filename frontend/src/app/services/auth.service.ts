import { Injectable, NgZone } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  surname: string;
  email: string;
  cellphone?: string;
  nickname: string;
  password: string;
  imagen?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface UserResponse {
  id: number;
  name: string;
  surname: string;
  email: string;
  cellphone?: string;
  nickname: string;
  userType: string;
  imagen?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface TwoFactorEnableRequest {
  username: string;
}

export interface TwoFactorEnableResponse {
  qrCode: string;
  recoveryCodes: string[];
}

export interface TwoFactorVerifyRequest {
  username: string;
  code: string;
}

export interface TwoFactorVerifyResponse {
  verified: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  
  private currentUserSubject = new BehaviorSubject<UserResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  
  private isLoggingOut = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private ngZone: NgZone
  ) {
    // Inicializar estado basado en token local sin hacer peticiones HTTP
    this.initializeAuthState();
  }

  /**
   * Inicializa el estado de autenticación basado en tokens locales
   */
  private initializeAuthState(): void {
    const token = this.getToken();
    if (token) {
      // Solo marcar como autenticado si hay token, sin hacer peticiones HTTP
      this.isAuthenticatedSubject.next(true);
    } else {
      // Asegurar que el estado esté limpio si no hay token
      this.clearAuthState();
    }
  }

  /**
   * Verifica si el usuario está autenticado haciendo una petición al servidor
   */
  private checkAuthStatus(): void {
    // No verificar si estamos en proceso de logout
    if (this.isLoggingOut) {
      return;
    }
    
    const token = this.getToken();
    if (token) {
      // Obtener el usuario actual y actualizar el estado
      this.http.get<UserResponse>(`${this.API_URL}/me`, {
        headers: this.getAuthHeaders()
      }).subscribe({
        next: (user) => {
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
        },
        error: () => {
          // Limpiar estado sin hacer logout recursivo
          this.clearAuthState();
        }
      });
    } else {
      // Asegurar que el estado esté limpio si no hay token
      this.clearAuthState();
    }
  }

  /**
   * Registra un nuevo usuario
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  /**
   * Inicia sesión con email y contraseña
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene los datos del usuario actual
   */
  getCurrentUser(): Observable<UserResponse> {
    // Si ya tenemos un usuario en el estado y es válido, lo devolvemos
    const currentUser = this.currentUserSubject.value;
    if (currentUser) {
      return new Observable<UserResponse>(observer => {
        observer.next(currentUser);
        observer.complete();
      });
    }
    
    // Si no hay usuario en el estado o es null, hacemos la petición
    return this.http.get<UserResponse>(`${this.API_URL}/me`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Inicializa los datos del usuario si están disponibles
   * Este método debe ser llamado por componentes que necesiten datos del usuario
   */
  initializeUserData(): void {
    // Solo cargar datos si hay token y no hay usuario cargado
    if (this.getToken() && !this.currentUserSubject.value) {
      this.checkAuthStatus();
    }
  }

  /**
   * Refresca el token de acceso
   */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    const request: RefreshTokenRequest = { refreshToken };
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  /**
   * Cierra la sesión del usuario
   */
  logout(): Observable<any> {
    this.isLoggingOut = true;
    const refreshToken = this.getRefreshToken();
    
    if (refreshToken) {
      const request: RefreshTokenRequest = { refreshToken };
      return this.http.post(`${this.API_URL}/logout`, request, {
        headers: this.getAuthHeaders()
      }).pipe(
        tap(() => this.handleLogout()),
        catchError(() => {
          this.handleLogout();
          return throwError(() => new Error('Logout failed'));
        })
      );
    } else {
      this.handleLogout();
      return new Observable(observer => {
        observer.next(null);
        observer.complete();
      });
    }
  }

  /**
   * Maneja la respuesta exitosa de autenticación
   */
  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
    
    // Actualizar estado de forma síncrona para evitar problemas de timing
    setTimeout(() => {
      this.currentUserSubject.next(response.user);
      this.isAuthenticatedSubject.next(true);
    }, 0);
  }

  /**
   * Limpia el estado de autenticación sin redirección
   */
  private clearAuthState(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Maneja el cierre de sesión
   */
  private handleLogout(): void {
    this.clearAuthState();
    this.isLoggingOut = false;
    
    // Usar NgZone para asegurar que la navegación se ejecute en el contexto de Angular
    this.ngZone.run(() => {
      this.router.navigate(['/home']).then(() => {
        // Forzar recarga de la página para asegurar que el estado se actualice completamente
        window.location.reload();
      });
    });
  }

  /**
   * Obtiene el token de acceso del localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtiene el refresh token del localStorage
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Verifica si el usuario está autenticado
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Obtiene el usuario actual
   */
  getCurrentUserValue(): UserResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Obtiene los headers de autenticación
   */
  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Actualiza el perfil del usuario
   */
  updateProfile(profileData: any): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.API_URL}/update-profile`, profileData, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Cambia la contraseña del usuario
   */
  changePassword(passwordData: { currentPassword: string; newPassword: string }): Observable<any> {
    return this.http.post(`${this.API_URL}/change-password`, passwordData, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Habilita 2FA para el usuario
   */
  enable2FA(username: string): Observable<TwoFactorEnableResponse> {
    const request: TwoFactorEnableRequest = { username };
    return this.http.post<TwoFactorEnableResponse>(`${environment.apiUrl}/api/2fa/enable`, request, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Verifica el código 2FA
   */
  verify2FA(username: string, code: string): Observable<TwoFactorVerifyResponse> {
    const request: TwoFactorVerifyRequest = { username, code };
    return this.http.post<TwoFactorVerifyResponse>(`${environment.apiUrl}/api/2fa/verify`, request, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Verifica código de recuperación 2FA
   */
  verifyRecovery2FA(username: string, recoveryCode: string): Observable<TwoFactorVerifyResponse> {
    return this.http.post<TwoFactorVerifyResponse>(`${environment.apiUrl}/api/2fa/verify-recovery/${username}`, recoveryCode, {
      headers: {
        ...this.getAuthHeaders(),
        'Content-Type': 'text/plain'
      }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Rota el secreto 2FA (regenera QR y códigos)
   */
  rotate2FA(username: string): Observable<TwoFactorEnableResponse> {
    return this.http.post<TwoFactorEnableResponse>(`${environment.apiUrl}/api/2fa/rotate/${username}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Deshabilita 2FA para el usuario
   */
  disable2FA(username: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/2fa/disable/${username}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene un usuario por su nickname
   */
  getUserByNickname(nickname: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.API_URL}/users/${nickname}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Maneja errores de HTTP
   */
  private handleError = (error: any): Observable<never> => {
    console.error('Auth Service Error:', error);
    
    if (error.status === 401) {
      this.logout();
    }
    
    return throwError(() => error);
  }
}