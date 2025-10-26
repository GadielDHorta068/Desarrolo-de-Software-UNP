import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventsTemp, EventType, EventTypes, RaffleCreate, RaffleNumber, StatusEvent } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { InfoModal, ModalInfo } from '../../shared/components/modal-info/modal-info';
import { InfoEvent } from '../../shared/components/info-event/info-event';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventShareCardComponent } from '../../shared/event-share-card/event-share-card.component';
import { AuthService } from '../../services/auth.service';
import { ModalShareEvent } from '../../shared/components/modal-share-event/modal-share-event';
import { QuestionaryComponent } from '../questionary/questionary.component';
import { EventsService } from '../../services/events.service';
import { UserDTO } from '../../models/UserDTO';
import { WinnersAudit } from '../../services/audit.service';
import { DataStatusEvent } from '../../models/response.model';
import { TagPrize } from '../../shared/components/tag-prize/tag-prize';
import { QuestionaryService } from '../../services/questionary.service';
import { NotificationService } from '../../services/notification.service';
import { BuyRaffleNumberDTO } from '../../models/buyRaffleNumberDTO';
import { RaffleNumbersComponent } from '../raffle-numbers.component/raffle-numbers.component';
import { AdminInscriptService } from '../../services/admin/adminInscript';

@Component({
    selector: 'app-management-event',
    imports: [CommonModule, RouterLink, ReactiveFormsModule, LoaderImage, ModalInfo, InfoEvent, HandleDatePipe,
        EventShareCardComponent, ModalShareEvent, RaffleNumbersComponent, QuestionaryComponent, TagPrize],
    templateUrl: './management-event.html',
    styleUrl: './management-event.css',
    providers: [HandleDatePipe]
})
export class ManagementEvent {
    @ViewChild('modalShareEvent') modalShareEvent!: ModalShareEvent;
    // @ViewChild('modalInfo') modalInfoRef!: ModalInfo;
    // dataModal: InfoModal = { title: "Actualización de datos", message: "" };

    // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
    event!: EventsTemp | null;
    // event!: EventsTemp|RaffleCreate|null;
    eventAux!: EventsTemp | null;
    imageEvent: File | null = null;
    eventIdParam!: Number | null;

    formEvent!: FormGroup;
    // tipos de sorteo
    types: EventType[] = [];
    // categorias de sorteo
    categories: Category[] = [];

    // PRUEBA QUESTIONARY MODAL
    showModalIncript = false;
    showRaffleModal = false;
    selectedEventId!: number;

    // TABS
    readonly TAB_INFO = 'info';
    readonly TAB_NUMBERS = 'numeros';
    readonly TAB_REGISTERED = 'registrados';
    tab: string = this.TAB_INFO;
    numeros: RaffleNumber[] = [];
    selectedRaffleNumbers: number[] = [];
    typesOfEventes = EventTypes;
    participants: UserDTO[] = [];
    eventType!: EventTypes;
    winnersAudit: WinnersAudit[] = [];

    constructor(
        private adminEventService: AdminEventService,
        private handleDatePipe: HandleDatePipe,
        private router: Router,
        private route: ActivatedRoute,
        private authService: AuthService,
        private eventService: EventsService,
        private cdr: ChangeDetectorRef,
        private questionaryService: QuestionaryService,
        private notificationService: NotificationService,
        private adminInscriptService: AdminInscriptService
    ) {
        this.adminEventService.selectedEvent$.subscribe(
            currentEvent => {
                this.eventAux = currentEvent ? { ...currentEvent } : null;
                this.event = currentEvent;
                // console.log("[admin-event] => evento seleccionado: ", this.event);
                if (this.event) {
                    this.initForm();
                    this.cdr.detectChanges();
                }
            }
        )

        this.adminEventService.winnersEvent$.subscribe(
            winners => {
                this.winnersAudit = winners;
                this.cdr.detectChanges();
            }
        )
    }

    ngOnInit() {
        this.eventIdParam = Number(this.route.snapshot.paramMap.get('eventId'));
        // console.log("[admin-event] => ide del evento recibido por param: ", this.eventIdParam);
        // revisamos si los datos del evento ya fueron seteados desde la lista de eventos
        if (!this.event) {
            this.eventService.getEventById("" + this.eventIdParam).subscribe(
                resp => {
                    // console.log("[admin-event] => evento recuperado por id de param: ", resp);
                    this.adminEventService.setSelectedEvent(resp);
                }
            )
        }
    }


    private initForm() {
        let dateEvent = this.event?.endDate ? this.handleDatePipe.transform(this.event?.endDate) : "";

        this.formEvent = new FormGroup({
            title: new FormControl({ value: this.event?.title, disabled: true }, { validators: [Validators.required] }),
            drawType: new FormControl({ value: this.event?.eventType, disabled: true }, { validators: [Validators.required] }),
            category: new FormControl({ value: this.event?.categoryId, disabled: false }),
            executionDate: new FormControl({ value: this.parseDate(dateEvent), disabled: false }, { validators: [Validators.required] }),
            winners: new FormControl({ value: this.event?.winnersCount, disabled: true }, { validators: [Validators.required] }),
            description: new FormControl({ value: this.event?.description, disabled: false }, { validators: [Validators.required] }),
            image: new FormControl({ value: null, disabled: false }),
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
        // return this.authService.isAuthenticated() && 
        //         !this.isUserCreator && 
        //         !this.event?.isUserRegistered &&
        //         this.event?.statusEvent === StatusEvent.OPEN;
        return !this.isUserCreator &&
            !this.event?.isUserRegistered &&
            this.event?.statusEvent === StatusEvent.OPEN;
    }

    // onInscript() {
    //     // controlamos que el evento este abierto
    //     this.eventService.getStatusEventById(""+this.event?.id).subscribe({
    //         next: (data) => {
    //             console.log('[estadoEvento] => estado del evento: ', data);
    //             const dataStatus: DataStatusEvent = data.data as DataStatusEvent;
    //             // this.dataModal.message = "Estado del evento: ", dataStatus.status;
    //             if(dataStatus.status === StatusEvent.OPEN){
    //                 // TODO: aca permitimos la inscripcion    
    //                 if (this.event?.id && this.event?.eventType === EventTypes.GIVEAWAY) {
    //                     // mostramos el form de inscripcion al sorteo
    //                     this.showModalIncript = true;
    //                 }
    //                 if (this.event?.id && this.event?.eventType === EventTypes.RAFFLES) {
    //                     try {
    //                         this.showRaffleModal = true;
    //                         // this.cdr.detectChanges();
    //                     } catch (err) {
    //                         console.error('ERROR dentro de onInscript (bloque RAFFLE):', err);
    //                     }
    //                 }
    //             }
    //             else{
    //                 if(this.event){
    //                     this.event.statusEvent = dataStatus.status as StatusEvent
    //                 }
    //             }
    //             // this.modalInfoRef.open();       // no muestra el estado, ver
    //             this.cdr.detectChanges();
    //         },
    //         error: (err) => {
    //             console.error('Error al obtener el estado del evento:', err);
    //         }
    //     })
    // }
    async onInscript(){
        const respStatus = await this.adminInscriptService.checkStatusEventToInscript();
        if(!respStatus){
            this.notificationService.notifyError("No fue posible realizar la operación");
        }
        else{
            if(respStatus != StatusEvent.OPEN){
                this.notificationService.notifyError("No fue posible realizar la operación. El evento se encuentra en estado: ", respStatus);
            }
        }
    }

    // onProceedToQuestionary(numbersToBuy: number[]): void {
    //     console.log('Numeros como parametro: ' + numbersToBuy);
    //     this.selectedRaffleNumbers = numbersToBuy;
    //     console.log('Numeros ya asignados: ' + this.selectedRaffleNumbers);

    //     this.showRaffleModal = false;
    //     this.showModalIncript = true;
    // }

    // onRaffleClosed(): void {
    //     this.showRaffleModal = false; // oculta el modal de rifa
    // }

    // onInscriptClosed(): void {
    //     this.showModalIncript = false; // Oculta modal de inscripcion
    // }

    // onQuestionarySubmit(user: UserDTO): void {
    //     if (!this.event) return;

    //     if (this.event.eventType === this.typesOfEventes.RAFFLES) {
    //         const buyNumRequest: BuyRaffleNumberDTO = {
    //             aGuestUser: user,
    //             someNumbersToBuy: this.selectedRaffleNumbers
    //         }
    //         this.questionaryService.saveRaffleNumber(
    //             this.event.id,
    //             buyNumRequest
    //         ).subscribe({
    //             next: (response) => {
    //                 this.notificationService.notifySuccess(response.message);
    //             },
    //             error: (errorResponse) => {
    //                 console.log('error 1');
    //                 this.notificationService.notifyError(errorResponse.error.message);
    //             }
    //         });
    //     }
    //     else {
    //         this.questionaryService.save(
    //             user,
    //             this.event.id
    //         ).subscribe({
    //             next: (response) => {
    //                 this.notificationService.notifySuccess(response.message);
    //             },
    //             error: (errorResponse) => {
    //                 console.log('LOG ERROR:', JSON.stringify(errorResponse)); // borrar
    //                 this.notificationService.notifyError(errorResponse.error.message);
    //             }
    //         });
    //     }

    // }
    
    allEventStates = StatusEvent;
    purchaseNumbers(): void {
        if (this.event?.statusEvent === this.allEventStates.OPEN) {
            const seleccionados = this.numeros.filter(n => n.selectStatus && !n.buyStatus);
            this.selectedRaffleNumbers = seleccionados.map(n => n.ticketNumber); // guardamos los números
            
            this.showModalIncript = true; // muestra el modal de Questionary
        }
    }

    loadParticipants(eventId: number, eventType: EventTypes): void {
        this.eventService.getParticipantUsersByEventId(eventId, eventType).subscribe({
            next: (data) => {
                this.participants = data;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error al obtener participantes:', err);
            }
        });
    }

    // devuelve el lugar en el podio
    getPlaceGoal(dataUser: UserDTO): any {
        // voy a buscar el dato de la lista de ganadores
        const dataPlace = this.winnersAudit.find(winner => winner.userEmail == dataUser.email);
        // console.log("[podio] => datos del ganador: ", dataPlace);
        if (!dataPlace)
            return { idUser: null, position: -1 }

        return { idUser: dataPlace.id, position: dataPlace.userPosition as number }
    }

    setTab(tabName: string): void {
        this.tab = tabName;
        if (tabName === this.TAB_REGISTERED && this.event?.id) {
            this.loadParticipants(this.event.id, this.event.eventType);
        }
    }
    
    // controles de pestaña
    isRegisteredTab(): boolean {
        return this.tab === this.TAB_REGISTERED;
    }

    isNumbersTab(): boolean {
        return this.tab === this.TAB_NUMBERS;
    }

    isInfoTab(): boolean {
        return this.tab === this.TAB_INFO;
    }

}
