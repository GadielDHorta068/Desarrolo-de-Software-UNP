import { AfterViewInit, ChangeDetectorRef, Component, input, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { EventsTemp, EventTypes, StatusEvent } from '../../../models/events.model';
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
import { InfoModal, ModalInfo } from '../modal-info/modal-info';
import { EventsService } from '../../../services/events.service';
import { NotificationService } from '../../../services/notification.service';
import { DataStatusEvent } from '../../../models/response.model';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule, HandleStatusPipe, HandleIconTypePipe, HandleDatePipe, ModalDrawInfo, QuestionaryComponent, ModalInfo],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('modalInfo') modalInfoRef!: ModalInfo;
  dataModal: InfoModal = {title: "Administración del evento", message: ""};

  userCurrent: UserResponse|null = null;
  // @Input() event!: Events|null;
  @Input() event!: EventsTemp|null;
  customBackground = input<string>('bg-white');

  // Estado del usuario
  currentUser: UserResponse | null = null;
  isAuthenticated = false;
  private authSubscription?: Subscription;
  isCreator: boolean = false;
  public StatusEvent = StatusEvent;

  // modal de inscripcion a sorteo
  showFormGiveaway = false; // el modal empieza desactivado
  selectedEventId!: number;

  constructor(
    private router: Router,
    private adminEventService: AdminEventService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private eventsService: EventsService,
    private notificationService: NotificationService,
    private eventService: EventsService
  ){
    this.userCurrent = this.authService.getCurrentUserValue();
    // console.log("[card-event] => usuario actual: ", this.userCurrent);
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
           !this.event?.isUserRegistered &&
           this.event?.statusEvent === StatusEvent.OPEN;
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

  // onInscript(){
  //   if(this.event?.id && this.event?.eventType == EventTypes.GIVEAWAY){
  //     // mostramos el form de inscripcion al sorteo
  //     this.selectedEventId = this.event.id;
  //     this.showFormGiveaway = true;
  //   }
  //   if(this.event?.id && this.event?.eventType == EventTypes.RAFFLES){
  //     alert("Aca iria el componente de seleccion de nros de rifa")
  //   }
  // }
  onInscript() {
    // controlamos que el evento este abierto
    this.eventService.getStatusEventById("" + this.event?.id).subscribe({
      next: (data) => {
        console.log('[drawCard] => estado del evento: ', data);
        const dataStatus: DataStatusEvent = data.data as DataStatusEvent;
        // this.dataModal.message = "Estado del evento: ", dataStatus.status;
        if (dataStatus.status == StatusEvent.OPEN) {
          // TODO: aca permitimos la inscripcion    
          if (this.event?.id && this.event?.eventType == EventTypes.GIVEAWAY) {
            // mostramos el form de inscripcion al sorteo
            this.selectedEventId = this.event?.id;
            this.showFormGiveaway = true;
          }
          if (this.event?.id && this.event?.eventType == EventTypes.RAFFLES) {
            alert("Aca iria el componente de seleccion de nros de rifa")
          }
        }
        else {
          if (this.event) {
            this.event.statusEvent = dataStatus.status as StatusEvent
          }
        }
        this.modalInfoRef.open();       // no muestra el estado, ver
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al obtener el estado del evento:', err);
        // console.error('Error al obtener el estado del evento:', err);
      }
    })
  }

  // modal de inscripcion a sorteos
  // openInscriptionGiveayas(aEventId?: number) {
  //   if (!aEventId) {
  //     console.warn("eventId inválido:", aEventId); //borrar
  //   return;
  //   }
  //   this.selectedEventId = aEventId;
  //   this.showFormGiveaway = true;
  // }

  ngAfterViewInit(){
    this.reviewCreator();
  }

  // public redirectEdit() {
  //   // console.log("[edit] => datos del evento: ", this.event);
  //   // controlamos q solo los de estado ABIERTO se puedan editar
  //   if(this.event?.statusEvent != StatusEvent.OPEN){
  //     this.dataModal.message = "No es posible editar el evento seleccionado. Solo se pueden editar los eventos en estaado ABIERTO";
  //     this.modalInfoRef.open();
  //     return;
  //   }
  //   // TODO: este control meterlo en el boton de edicion del card!!
  //   if(this.event?.eventType !== EventTypes.GIVEAWAY){
  //     this.dataModal.message = "Por el momento no es posible editar los datos de este tipo de eventos.";
  //     this.modalInfoRef.open();
  //     return;
  //   }
  //   this.adminEventService.setSelectedEvent(this.event);
  //   this.router.navigate(['/event-edit']);
  // }

  public redirectAdmin(){
    console.log("[onAdmin] => evento seleccionado: ", this.event);
    this.adminEventService.setSelectedEvent(this.event);
    // this.router.navigate(['/event-admin']);
    this.router.navigate([`/event/management/${this.event?.id}`]);
  }

  public onIncript(){
    console.log("Presiona incribirse!");
    alert("Se apreto INCRIBIRME");
  }

  // Cerrar inscripciones (solo creador)
  public closeRegistrations(): void {
    if (!this.isUserCreator || !this.event?.id) return;
    if (!this.userCurrent?.id) return;
    this.eventsService.updateEventStatus(this.event.id, this.userCurrent.id, StatusEvent.CLOSED).subscribe({
      next: (updated) => {
        if (this.event) {
          this.event.statusEvent = StatusEvent.CLOSED;
        }
        this.notificationService.notifySuccess('Inscripciones cerradas');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cerrar inscripciones:', err);
        this.notificationService.notifyError('No se pudo cerrar inscripciones');
      }
    });
  }

  // Finalizar evento (solo creador)
  public finalizeEvent(): void {
    if (!this.isUserCreator || !this.event?.id) return;
    // Navegar a la pantalla de ruleta de ganadores; el backend finalizará el evento.
    this.router.navigate(['/winners', this.event.id]);
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
