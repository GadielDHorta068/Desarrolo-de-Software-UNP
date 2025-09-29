import { AfterViewInit, ChangeDetectorRef, Component, input, Input, OnInit, OnDestroy } from '@angular/core';
import { Events, EventsTemp } from '../../../models/events.model';
import { CommonModule } from '@angular/common';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { ModalDrawInfo } from '../modal-draw-info/modal-draw-info';
import { Router } from '@angular/router';
import { AdminEventService } from '../../../services/admin/adminEvent.service';
import { QuestionaryComponent } from '../../../pages/questionary/questionary.component';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { AuthService, UserResponse } from '../../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule, HandleStatusPipe, HandleIconTypePipe, HandleDatePipe, ModalDrawInfo, QuestionaryComponent],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard implements OnInit, OnDestroy, AfterViewInit {

  userCurrent: UserResponse|null = null;
  // @Input() event!: Events|null;
  @Input() event!: EventsTemp|null;
  customBackground = input<string>('bg-white');

  // Estado del usuario
  currentUser: UserResponse | null = null;
  isAuthenticated = false;
  private authSubscription?: Subscription;
  isCreator: boolean = false;

  constructor(
    private router: Router,
    private adminEventService: AdminEventService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ){
    this.userCurrent = this.authService.getCurrentUserValue();
    console.log("[card-event] => usuario actual: ", this.userCurrent);
  }

  ngOnInit() {
    // Suscribirse al estado de autenticación
    this.authSubscription = this.authService.isAuthenticated$.subscribe(
      isAuth => {
        this.isAuthenticated = isAuth;
        if (isAuth) {
          this.authService.getCurrentUser().subscribe({
            next: (user) => {
              this.currentUser = user;
            },
            error: (error) => {
              console.error('Error obteniendo usuario actual:', error);
              this.currentUser = null;
            }
          });
        } else {
          this.currentUser = null;
        }
      }
    );
  }

  ngOnDestroy() {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  // Métodos para determinar qué botones mostrar
  get isUserCreator(): boolean {
    return this.currentUser?.id === this.event?.creator?.id;
  }

  get canUserRegister(): boolean {
    // El usuario puede registrarse si:
    // 1. Está autenticado
    // 2. No es el creador del evento
    // 3. No está ya registrado
    return this.isAuthenticated && 
           !this.isUserCreator && 
           !this.event?.isUserRegistered;
  }

  get isUserRegistered(): boolean {
    return this.event?.isUserRegistered === true;
  }

  get showEditButton(): boolean {
    // Solo el creador puede ver el botón de editar
    return this.isUserCreator;
  }

  get showRegisterButton(): boolean {
    // Mostrar botón de registro si el usuario puede registrarse
    return this.canUserRegister;
  }

  get showRegisteredStatus(): boolean {
    // Mostrar estado "Inscrito" si el usuario está registrado
    return this.isUserRegistered;
  }

  // PRUEBA QUESTIONARY MODAL
    showModal = false; // el modal empieza desactivado
    selectedEventId!: number;

    openModal(aEventId?: number) {
        if (!aEventId) {
        console.warn("eventId inválido:", aEventId); //borrar
        return;
        }
        this.selectedEventId = aEventId;
        this.showModal = true;
    }
  // PRUEBA QUESTIONARY MODAL

  ngAfterViewInit(){
    this.reviewCreator();
  }

  public redirectEdit() {
    this.adminEventService.setSelectedEvent(this.event);
    this.router.navigate(['/event-edit']);
  }

  public onIncript(){
    console.log("Presiona incribirse!");
    alert("Se apreto INCRIBIRME");
  }

  private reviewCreator(){
    // console.log("[card-event] => datos del usuario: ", this.userCurrent);
    // console.log("[card-event] => datos del evento: ", this.event);
    // console.log("[card-event] => es el creador: ", (this.userCurrent?.id == this.event?.creator.id));
    
    if( this.userCurrent?.id && this.event?.creator.id && (this.userCurrent?.id == this.event?.creator.id)){
      this.isCreator = true;
    }
    else{
      this.isCreator = false;
    }
    this.cdr.detectChanges();
  }
  
}
