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

@Component({
    selector: 'app-management-event',
    imports: [CommonModule, RouterLink, ReactiveFormsModule, LoaderImage, ModalInfo, InfoEvent, HandleDatePipe,
            EventShareCardComponent, ModalShareEvent, QuestionaryComponent, TagPrize],
    templateUrl: './management-event.html',
    styleUrl: './management-event.css',
    providers: [HandleDatePipe]
})
export class ManagementEvent {
    @ViewChild('modalShareEvent') modalShareEvent!: ModalShareEvent;
    @ViewChild('modalInfo') modalInfoRef!: ModalInfo;
    dataModal: InfoModal = { title: "Actualización de datos", message: "" };

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
    selectedEventId?: number;

    // TABS
    readonly TAB_INFO = 'info';
    readonly TAB_NUMBERS = 'numeros';
    readonly TAB_REGISTERED = 'registrados';
    tab: string = this.TAB_INFO;
    numeros: RaffleNumber[] = [];
    selectedNumbers: number[] = [];
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
        private cdr: ChangeDetectorRef
    ) {
        this.adminEventService.selectedEvent$.subscribe(
            currentEvent => {
                this.eventAux = currentEvent ? { ...currentEvent } : null;
                this.event = currentEvent;
                // console.log("[admin-event] => evento seleccionado: ", this.event);
                if (this.event) {
                    this.initForm();
                    this.initRaffleNumbers();
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
                    // this.event = resp;
                    // if (this.event) {
                    //     this.initForm();
                    //     this.initRaffleNumbers();
                    //     this.cdr.detectChanges();
                    // }

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

    onInscript() {
        // controlamos que el evento este abierto
        this.eventService.getStatusEventById(""+this.event?.id).subscribe({
            next: (data) => {
                console.log('[estadoEvento] => estado del evento: ', data);
                const dataStatus: DataStatusEvent = data.data as DataStatusEvent;
                this.dataModal.message = "Estado del evento: ", dataStatus.status;
                console.log('[estadoEvento] => el estado del evento es: ', dataStatus.status);
                // TODO: seguir aca
                if(dataStatus.status != StatusEvent.OPEN){
                    // TODO: aca dejar inscribirse!!    
                }
                this.modalInfoRef.open();       // no muestra el estado, ver
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error al obtener el estado del evento:', err);
                // console.error('Error al obtener el estado del evento:', err);
            }
        })
        return;

        if (this.event?.id && this.event?.eventType == EventTypes.GIVEAWAY) {
            // mostramos el form de inscripcion al sorteo
            this.selectedEventId = this.event?.id;
            this.showModalIncript = true;
        }
        if (this.event?.id && this.event?.eventType == EventTypes.RAFFLES) {
            alert("Aca iria el componente de seleccion de nros de rifa")
        }
    }

    // inicializamos la grilla de numeros de las rifas
    private initRaffleNumbers(): void {
        if (!this.event) {
            //   console.warn('[Raffle] No hay evento cargado aún.');
            return;
        }


        if (this.event.eventType !== EventTypes.RAFFLES) {
            //   console.log('[Raffle] El evento no es tipo RAFFLES. No se generan números.');
            return;
        }

        const total = this.event.quantityOfNumbers;
        if (!total || total <= 0) {
            console.warn('[Raffle] quantityOfNumbers inválido:', total);
            this.numeros = [];
            return;
        }


        this.eventService.getSoldNumbersByRaffleId(this.event.id).subscribe({
            next: (boughtNumbers: number[]) => {

                this.numeros = Array.from({ length: total }, (_, i) => ({
                    ticketNumber: i + 1,
                    buyStatus: boughtNumbers.includes(i + 1),
                    selectStatus: false
                }));

                this.cdr.detectChanges(); // forzamos render
            },
            error: (err) => {
                console.error('[Raffle] Error al obtener los números vendidos:', err);
                // aunque haya error, podemos inicializar un array vacío para no romper la UI
                this.numeros = Array.from({ length: total }, (_, i) => ({
                    ticketNumber: i + 1,
                    buyStatus: false,
                    selectStatus: false
                }));
                this.cdr.detectChanges();
            }
        });
    }


    selectNumber(aRaffleNumber: RaffleNumber): void {
        if (!aRaffleNumber.buyStatus) {
            aRaffleNumber.selectStatus = !aRaffleNumber.selectStatus;
        }
    }

    addToCart(): void {
        if (this.event?.id) {
            const seleccionados = this.numeros.filter(n => n.selectStatus && !n.buyStatus);

            this.selectedEventId = this.event.id;
            this.eventType = this.event.eventType;
            this.selectedNumbers = seleccionados.map(n => n.ticketNumber); // 👈 guardamos los números
            this.showModalIncript = true; // muestra el modal de Questionary
        }
    }

    onModalClosed(): void {
        this.showModalIncript = false; // oculta el modal
        setTimeout(() => this.initRaffleNumbers(), 500); // refresca los números
    }

    loadParticipants(eventId: number, eventType: EventTypes): void {
        this.eventService.getParticipantUsersByEventId(eventId, eventType).subscribe({
            next: (data) => {
                this.participants = data;
                console.log('[Participantes cargados]', data);
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

        // POR AHORA SOLO HASTA EL 3er premio
        // if (dataPlace.userPosition == 1) {
        //     return { goal: "1er PREMIO", idUser: dataPlace.id, position: ""+dataPlace.userPosition }
        // }
        // if (dataPlace.userPosition == 2) {
        //     return { goal: "2do PREMIO", idUser: dataPlace.id, position: ""+dataPlace.userPosition }
        // }
        // if (dataPlace.userPosition == 3) {
        //     return { goal: "#er PREMIO", idUser: dataPlace.id, position: ""+dataPlace.userPosition }
        // }
        // return null;
        // return dataPlace.userPosition as number;
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
