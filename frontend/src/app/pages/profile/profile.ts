import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, UserResponse } from '../../services/auth.service';
import { EventsService } from '../../services/events.service';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { Events } from '../../models/events.model';
import { ActivatedRoute, Router } from '@angular/router';
import { ClipboardModule } from '@angular/cdk/clipboard';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, HttpClientModule, ClipboardModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit, OnDestroy {
  userProfile: UserResponse | null = null;
  userEvents: Events[] = [];
  loading = true;
  error = '';
  showCopiedMessage = false;
  private subscription: Subscription | null = null;
  private eventsSubscription: Subscription | null = null;
  
  // Getter para manejar la imagen del perfil
  get profileImageSrc(): string {
    if (this.userProfile?.imagen) {
      // Si la imagen ya tiene el prefijo data:, la devolvemos tal como está
      if (this.userProfile.imagen.startsWith('data:')) {
        return this.userProfile.imagen;
      }
      // Si es solo base64, agregamos el prefijo
      return `data:image/jpeg;base64,${this.userProfile.imagen}`;
    }
    // Imagen por defecto si no hay imagen
    return 'assets/default-profile.jpg';
  }

  constructor(
    private authService: AuthService,
    private eventsService: EventsService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Inicializar datos del usuario si están disponibles
    this.authService.initializeUserData();
    
    this.route.params.subscribe(params => {
      const nickname = params['nickname'];
      if (nickname) {
        // Cargar perfil por nickname
        this.loadUserByNickname(nickname);
      } else {
        // Cargar perfil del usuario actual
        const currentUser = this.authService.getCurrentUserValue();
        if (currentUser) {
          this.userProfile = currentUser;
          this.loading = false;
          this.loadUserEvents(currentUser.id);
        } else {
          this.loadUserProfile();
        }
      }
    });
  }

  private loadUserByNickname(nickname: string) {
    this.loading = true;
    this.error = '';
    this.authService.getUserByNickname(nickname).subscribe({
      next: (user) => {
        if (user) {
          // Limpiar espacios en blanco de la URL de la imagen
          if (user.imagen) {
            user.imagen = user.imagen.trim();
          }
          this.userProfile = user;
          this.loadUserEvents(user.id);
          this.cdr.detectChanges();
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading user profile:', err);
        this.error = 'Error al cargar el perfil del usuario';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  copyProfileUrl() {
    if (this.userProfile) {
      const url = `${window.location.origin}/profile/${this.userProfile.nickname}`;
      navigator.clipboard.writeText(url).then(() => {
        this.showCopiedMessage = true;
        setTimeout(() => {
          this.showCopiedMessage = false;
          this.cdr.detectChanges();
        }, 2000);
        this.cdr.detectChanges();
      }).catch(() => {
        console.error('Error al copiar URL');
      });
    }
  }

  private loadUserProfile() {
    this.loading = true;
    this.error = '';
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.subscription = this.authService.getCurrentUser().subscribe({
      next: (user) => {
        if (user) {
          this.userProfile = user;
          this.loading = false;
          this.loadUserEvents(user.id);
        } else {
          this.error = 'No se pudo cargar el perfil del usuario';
          this.loading = false;
        }
      },
      error: (err: Error) => {
        console.error('Error loading user profile:', err);
        this.error = 'Error al cargar el perfil del usuario';
        this.loading = false;
      }
    });
  }

  private loadUserEvents(userId: number) {
    this.eventsService.getEventsByParticipantId(userId).subscribe({
      next: (events) => {
        this.userEvents = events;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading user events:', err);
        // Inicializar eventos como array vacío para evitar errores en la vista
        this.userEvents = [];
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe();
    }
  }
}
