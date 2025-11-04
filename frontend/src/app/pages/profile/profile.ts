import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { AuthService, UserResponse } from '../../services/auth.service';
import { EventsService } from '../../services/events.service';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { Events, EventsTemp } from '../../models/events.model';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { HandleStatusPipe } from '../../pipes/handle-status.pipe';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, HttpClientModule, ClipboardModule, DrawCard, RouterModule, HandleStatusPipe],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
  animations: [
    trigger('tabTransition', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(8px)' }),
        animate('220ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('160ms ease-in', style({ opacity: 0, transform: 'translateY(-6px)' }))
      ])
    ])
  ]
})
export class Profile implements OnInit, OnDestroy {
  userProfile: UserResponse | null = null;
  // userEvents: Events[] = [];
  userEvents: EventsTemp[] = [];
  // eventos donde el usuario está inscrito (historial)
  joinedEvents: Events[] = [];
  joinedLoading = false;
  joinedError = '';

  // tabs
  activeTab: 'informacion' | 'mis-eventos' | 'historial' = 'informacion';

  loading = true;
  error = '';
  showCopiedMessage = false;
  showCopiedFieldMessage = false;
  private subscription: Subscription | null = null;
  private eventsSubscription: Subscription | null = null;
  private joinedSubscription: Subscription | null = null;
  
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
          this.loadUserEventsByCreator(currentUser.id);
          this.loadUserJoinedEvents(currentUser.id);
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
          this.loadUserEventsByCreator(user.id);
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

  copyNickname() {
    const nick = this.userProfile?.nickname;
    if (!nick) return;
    navigator.clipboard.writeText(`@${nick}`).then(() => {
      this.showCopiedFieldMessage = true;
      setTimeout(() => {
        this.showCopiedFieldMessage = false;
        this.cdr.detectChanges();
      }, 1500);
      this.cdr.detectChanges();
    }).catch(() => console.error('Error al copiar nickname'));
  }

  copyEmail() {
    const email = this.userProfile?.email;
    if (!email) return;
    navigator.clipboard.writeText(email).then(() => {
      this.showCopiedFieldMessage = true;
      setTimeout(() => {
        this.showCopiedFieldMessage = false;
        this.cdr.detectChanges();
      }, 1500);
      this.cdr.detectChanges();
    }).catch(() => console.error('Error al copiar email'));
  }

  copyCellphone() {
    const phone = this.userProfile?.cellphone;
    if (!phone) return;
    navigator.clipboard.writeText(phone).then(() => {
      this.showCopiedFieldMessage = true;
      setTimeout(() => {
        this.showCopiedFieldMessage = false;
        this.cdr.detectChanges();
      }, 1500);
      this.cdr.detectChanges();
    }).catch(() => console.error('Error al copiar celular'));
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
          this.loadUserEventsByCreator(user.id);
          this.loadUserJoinedEvents(user.id);
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

  private loadUserEventsByCreator(userId: number) {
    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe();
    }
    this.eventsSubscription = this.eventsService.getAllByCreator(userId.toString()).subscribe({
      next: (events) => {
        this.userEvents = events;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading creator events:', err);
        // Inicializar eventos como array vacío para evitar errores en la vista
        this.userEvents = [];
        this.cdr.detectChanges();
      }
    });
  }

  private loadUserJoinedEvents(userId: number) {
    // Cargar eventos donde el usuario está inscrito
    if (this.joinedSubscription) {
      this.joinedSubscription.unsubscribe();
    }
    this.joinedLoading = true;
    this.joinedError = '';
    this.joinedSubscription = this.eventsService.getEventsByParticipantId(userId).subscribe({
      next: (events) => {
        this.joinedEvents = events || [];
        this.joinedLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading joined events:', err);
        this.joinedEvents = [];
        this.joinedLoading = false;
        this.joinedError = 'No se pudo cargar el historial de eventos';
        this.cdr.detectChanges();
      }
    });
  }

  selectTab(tab: 'informacion' | 'mis-eventos' | 'historial') {
    this.activeTab = tab;
    // Lazy-load historial si aún no está cargado
    if (tab === 'historial' && this.joinedEvents.length === 0 && this.userProfile?.id) {
      this.loadUserJoinedEvents(this.userProfile.id);
    }
    this.cdr.detectChanges();
  }

  // Link directo a WhatsApp con el celular del usuario
  getWhatsAppLink(): string {
    const phone = this.userProfile?.cellphone || '';
    const digits = phone.replace(/\D/g, '');
    if (!digits) return '';
    return `https://wa.me/${digits}`;
  }

  // Copiar descripción al portapapeles
  copyDescription() {
    const description = this.userProfile?.description;
    if (!description) return;
    navigator.clipboard.writeText(description).then(() => {
      this.showCopiedFieldMessage = true;
      setTimeout(() => {
        this.showCopiedFieldMessage = false;
        this.cdr.detectChanges();
      }, 1500);
      this.cdr.detectChanges();
    }).catch(() => console.error('Error al copiar descripción'));
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe();
    }
    if (this.joinedSubscription) {
      this.joinedSubscription.unsubscribe();
    }
  }
}
