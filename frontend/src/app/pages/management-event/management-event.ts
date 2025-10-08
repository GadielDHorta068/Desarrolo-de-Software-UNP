import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventsTemp, EventType, EventTypes, StatusEvent } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { InfoModal, ModalInfo } from '../../shared/components/modal-info/modal-info';
import { InfoEvent } from '../../shared/components/info-event/info-event';
import { Router, RouterLink } from '@angular/router';
import { EventShareCardComponent } from '../../shared/event-share-card/event-share-card.component';
import { AuthService } from '../../services/auth.service';
import { ModalShareEvent } from '../../shared/components/modal-share-event/modal-share-event';
import { QuestionaryComponent } from '../questionary/questionary.component';

@Component({
  selector: 'app-management-event',
  imports: [CommonModule, RouterLink, ReactiveFormsModule, LoaderImage, ModalInfo, InfoEvent, HandleDatePipe, EventShareCardComponent, ModalShareEvent, QuestionaryComponent],
  templateUrl: './management-event.html',
  styleUrl: './management-event.css',
  providers: [HandleDatePipe]
})
export class ManagementEvent {
  @ViewChild('modalShareEvent') modalShareEvent!: ModalShareEvent;
  @ViewChild('modalInfo') modalInfoRef!: ModalInfo;
  dataModal: InfoModal = {title: "Actualización de datos", message: ""};

  // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
  // event!: Events|null;
  event!: EventsTemp|null;
  eventAux!: EventsTemp|null;
  imageEvent: File|null = null;

  formEvent!: FormGroup;
  // tipos de sorteo
  types: EventType[] = [];
  // categorias de sorteo
  categories: Category[] = [];

  // PRUEBA QUESTIONARY MODAL
  showModalIncript = false;
  selectedEventId!: number;

  constructor(
    private adminEventService: AdminEventService,
    private handleDatePipe: HandleDatePipe,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ){
    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.eventAux = currentEvent ? {...currentEvent}: null;
        this.event = currentEvent;
        console.log("[edicion] => evento seleccionado: ", this.event);
        this.initForm();
      }
    )
  }


  private initForm(){
    let dateEvent = this.event?.endDate ? this.handleDatePipe.transform(this.event?.endDate): "";

    this.formEvent = new FormGroup({
      title: new FormControl({value: this.event?.title, disabled: true}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: this.event?.eventType, disabled: true}, {validators:[ Validators.required ]}),
      category: new FormControl({value: this.event?.categoryId, disabled: false}),
      executionDate: new FormControl({value: this.parseDate(dateEvent), disabled: false}, {validators:[ Validators.required ]}),
      winners: new FormControl({value: this.event?.winnersCount, disabled: true}, {validators:[ Validators.required ]}),
      description: new FormControl({value: this.event?.description, disabled: false}, {validators:[ Validators.required ]}),
      image: new FormControl({value: null, disabled: false}),
    });
    this.formEvent.disable();
  }

  private parseDate(fecha: string): string {
    const [dia, mes, anio] = fecha.split('-');

    const diaFormateado = dia.padStart(2, '0');
    const mesFormateado = mes.padStart(2, '0');

    return `${anio}-${mesFormateado}-${diaFormateado}`;
  }

  // Métodos para determinar qué botones mostrar
  get canEdit(): boolean {
    return this.authService.isAuthenticated() && this.isUserCreator;
  }

  // Métodos para determinar qué botones mostrar
  get isUserCreator(): boolean {
    return this.authService.getCurrentUserValue()?.id === this.event?.creator?.id;
  }

  get canUserInscript(): boolean {
    // El usuario puede inscribirse si:
    // 1. Está autenticado
    // 2. No es el creador del evento
    // 3. No está ya registrado
    return this.authService.isAuthenticated() && 
            !this.isUserCreator && 
            !this.event?.isUserRegistered &&
            this.event?.statusEvent === StatusEvent.OPEN;
  }

  onInscript(){
    if(this.event?.id && this.event?.eventType == EventTypes.GIVEAWAY){
      // mostramos el form de inscripcion al sorteo
      this.selectedEventId = this.event.id;
      this.showModalIncript = true;
    }
    if(this.event?.id && this.event?.eventType == EventTypes.RAFFLES){
      alert("Aca iria el componente de seleccion de nros de rifa")
    }
  }

}
