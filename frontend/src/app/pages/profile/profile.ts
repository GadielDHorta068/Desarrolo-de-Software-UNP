import { Component, OnInit, OnDestroy, ChangeDetectorRef, Inject } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { CommonModule, DOCUMENT } from '@angular/common';
import { AuthService, UserResponse } from '../../services/auth.service';
import { EventsService } from '../../services/events.service';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { Events, EventsTemp } from '../../models/events.model';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { HandleStatusPipe } from '../../pipes/handle-status.pipe';
import { Meta, Title } from '@angular/platform-browser';

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
  
  // Personalización visual y métricas
  accentColor: string = '#10b981'; // emerald por defecto
  coverImage: string | null = null;
  bannerBackground: string = 'linear-gradient(90deg, #0f172a, #1f2937)';
  createdEventsCount = 0;
  participatedEventsCount = 0;
  winnersTotalCount = 0;
  followersCount = 0;
  followingCount = 0;
  isFollowing = false;
  viewerId: number | null = null;
  // Listas y modales
  followersNicknames: string[] = [];
  followingNicknames: string[] = [];
  showFollowersModal = false;
  showFollowingModal = false;
  listLoading = false;
  listError = '';
  
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

  // Mostrar controles solo si el usuario actual es propietario del perfil
  get isOwner(): boolean {
    return this.viewerId != null && this.userProfile?.id === this.viewerId;
  }

  constructor(
    private authService: AuthService,
    private eventsService: EventsService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private meta: Meta,
    private title: Title,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit() {
    // Inicializar datos del usuario si están disponibles
    this.authService.initializeUserData();
    const viewer = this.authService.getCurrentUserValue();
    this.viewerId = viewer?.id ?? null;
    
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
          this.loadCustomization();
          this.updateMetaTags();
          this.loadFollowState();
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
          this.loadCustomization();
          this.updateMetaTags();
          this.loadFollowState();
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

  // Compartir perfil en redes o mediante Web Share API
  shareProfile() {
    if (!this.userProfile) return;
    const url = `${window.location.origin}/profile/${this.userProfile.nickname}`;
    const text = `Conoce el perfil de @${this.userProfile.nickname} en Raffy`;
    if (navigator.share) {
      navigator.share({ title: 'Perfil en Raffy', text, url }).catch(() => {
        // Fallback si el usuario cancela
      });
    } else {
      const encodedUrl = encodeURIComponent(url);
      const encodedText = encodeURIComponent(text);
      const twitter = `https://twitter.com/intent/tweet?url=${encodedUrl}&text=${encodedText}`;
      const facebook = `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`;
      const linkedin = `https://www.linkedin.com/sharing/share-offsite/?url=${encodedUrl}`;
      window.open(twitter, '_blank');
      window.open(facebook, '_blank');
      window.open(linkedin, '_blank');
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
          this.loadCustomization();
          this.updateMetaTags();
          this.loadFollowState();
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

  // --- Meta tags dinámicos para compartir perfil ---
  private updateMetaTags() {
    if (!this.userProfile) return;

    const fullName = `${this.userProfile.name} ${this.userProfile.surname}`.trim();
    const nickname = this.userProfile.nickname ? `@${this.userProfile.nickname}` : '';
    const title = `${fullName} ${nickname} • Perfil en Raffy`;
    const description = this.userProfile.description || 'Perfil en Raffy';
    const url = `${window.location.origin}/profile/${this.userProfile.nickname || ''}`;
    const image = this.profileImageSrc; // puede ser data: URI o asset por defecto

    // Actualizar título del documento
    this.title.setTitle(title);

    // Canonical link
    let canonical: HTMLLinkElement | null = this.document.querySelector('link[rel="canonical"]');
    if (!canonical) {
      canonical = this.document.createElement('link');
      canonical.setAttribute('rel', 'canonical');
      this.document.head.appendChild(canonical);
    }
    canonical.setAttribute('href', url);

    // Meta genérico
    this.meta.updateTag({ name: 'description', content: description });

    // Open Graph
    this.meta.updateTag({ property: 'og:title', content: title });
    this.meta.updateTag({ property: 'og:type', content: 'profile' });
    this.meta.updateTag({ property: 'og:url', content: url });
    this.meta.updateTag({ property: 'og:image', content: image });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ property: 'og:site_name', content: 'Raffy' });
    this.meta.updateTag({ property: 'profile:first_name', content: this.userProfile.name || '' });
    this.meta.updateTag({ property: 'profile:last_name', content: this.userProfile.surname || '' });
    if (this.userProfile.nickname) {
      this.meta.updateTag({ property: 'profile:username', content: this.userProfile.nickname });
    }

    // Twitter Cards
    this.meta.updateTag({ name: 'twitter:card', content: 'summary_large_image' });
    this.meta.updateTag({ name: 'twitter:title', content: title });
    this.meta.updateTag({ name: 'twitter:description', content: description });
    this.meta.updateTag({ name: 'twitter:image', content: image });
  }

  private loadUserEventsByCreator(userId: number) {
    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe();
    }
    this.eventsSubscription = this.eventsService.getAllByCreator(userId.toString()).subscribe({
      next: (events) => {
        this.userEvents = events;
        this.createdEventsCount = events?.length || 0;
        this.winnersTotalCount = (events || []).reduce((sum, e) => sum + (e.winnersCount || 0), 0);
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading creator events:', err);
        // Inicializar eventos como array vacío para evitar errores en la vista
        this.userEvents = [];
        this.createdEventsCount = 0;
        this.winnersTotalCount = 0;
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
        this.participatedEventsCount = this.joinedEvents.length;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Error loading joined events:', err);
        this.joinedEvents = [];
        this.joinedLoading = false;
        this.joinedError = 'No se pudo cargar el historial de eventos';
        this.participatedEventsCount = 0;
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

  // --- Personalización y seguidores (localStorage) ---
  private loadCustomization() {
    if (!this.userProfile) return;
    try {
      const raw = localStorage.getItem('profile_customizations');
      const map = raw ? JSON.parse(raw) : {};
      const key = String(this.userProfile.id);
      const cfg = map[key] || {};
      this.accentColor = cfg.accentColor || this.accentColor;
      // Preferir la portada proveniente del backend si existe
      const backendCover = this.userProfile.coverImage || null;
      this.coverImage = backendCover ? backendCover : (cfg.coverImage || null);
      // Construir fondo de banner
      if (this.coverImage) {
        const src = this.coverImage.startsWith('data:') ? this.coverImage : `data:image/jpeg;base64,${this.coverImage}`;
        this.bannerBackground = `url('${src}')`;
      } else {
        this.bannerBackground = `linear-gradient(120deg, ${this.accentColor}33, ${this.accentColor})`;
      }
    } catch (e) {
      console.warn('No se pudo cargar customización de perfil', e);
    }
  }

  private loadFollowState() {
    if (!this.userProfile) return;
    const targetId = this.userProfile.id;
    // Cargar conteo de seguidores desde backend
    this.authService.getFollowersCount(targetId).subscribe({
      next: (count) => {
        this.followersCount = count || 0;
        this.cdr.detectChanges();
      },
      error: () => {
        // Fallback suave si hay un error
        this.followersCount = 0;
      }
    });
    // Cargar conteo de seguidos
    this.authService.getFollowingCount(targetId).subscribe({
      next: (count) => {
        this.followingCount = count || 0;
        this.cdr.detectChanges();
      },
      error: () => {
        this.followingCount = 0;
      }
    });
    // Estado de seguimiento del usuario actual
    if (this.viewerId != null) {
      this.authService.isFollowing(targetId).subscribe({
        next: (following) => {
          this.isFollowing = !!following;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isFollowing = false;
        }
      });
    } else {
      this.isFollowing = false;
    }
  }

  openFollowersModal() {
    if (!this.userProfile) return;
    this.listLoading = true;
    this.listError = '';
    this.showFollowersModal = true;
    this.authService.getFollowersNicknames(this.userProfile.id).subscribe({
      next: (list) => {
        this.followersNicknames = list || [];
        this.listLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.followersNicknames = [];
        this.listError = 'No se pudo cargar la lista de seguidores';
        this.listLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openFollowingModal() {
    if (!this.userProfile) return;
    this.listLoading = true;
    this.listError = '';
    this.showFollowingModal = true;
    this.authService.getFollowingNicknames(this.userProfile.id).subscribe({
      next: (list) => {
        this.followingNicknames = list || [];
        this.listLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.followingNicknames = [];
        this.listError = 'No se pudo cargar la lista de seguidos';
        this.listLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  closeModal() {
    this.showFollowersModal = false;
    this.showFollowingModal = false;
    this.listLoading = false;
    this.listError = '';
  }

  goToProfile(nickname: string) {
    if (!nickname) return;
    this.closeModal();
    this.cdr.detectChanges();
    this.router.navigate(['/profile', nickname]);
  }

  toggleFollow() {
    if (!this.userProfile || this.viewerId == null) return;
    const targetId = this.userProfile.id;
    if (this.isFollowing) {
      // Unfollow
      this.authService.unfollowUser(targetId).subscribe({
        next: (count) => {
          this.isFollowing = false;
          this.followersCount = count || 0;
          this.cdr.detectChanges();
        },
        error: () => {
          // Mantener estado si falla
        }
      });
    } else {
      // Follow
      this.authService.followUser(targetId).subscribe({
        next: (count) => {
          this.isFollowing = true;
          this.followersCount = count || 0;
          this.cdr.detectChanges();
        },
        error: () => {
          // Mantener estado si falla
        }
      });
    }
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
