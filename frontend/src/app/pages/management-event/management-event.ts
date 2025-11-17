import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventsTemp, EventType, EventTypes, RaffleNumber, RaffleParticipantDTO, StatusEvent } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { ModalInfo } from '../../shared/components/modal-info/modal-info';
import { InfoEvent } from '../../shared/components/info-event/info-event';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventShareCardComponent } from '../../shared/event-share-card/event-share-card.component';
import { AuthService } from '../../services/auth.service';
import { ModalShareEvent } from '../../shared/components/modal-share-event/modal-share-event';
import { QuestionaryComponent } from '../questionary/questionary.component';
import { EventsService } from '../../services/events.service';
import { UserDTO } from '../../models/UserDTO';
import { WinnerDTO } from '../../models/winner.model';
import { TagPrize } from '../../shared/components/tag-prize/tag-prize';
import { NotificationService } from '../../services/notification.service';
import { RaffleNumbersComponent } from '../raffle-numbers.component/raffle-numbers.component';
import { AdminInscriptService } from '../../services/admin/adminInscript';
import { InviteLoginComponent } from '../../shared/components/invite-login/invite-login.component';
import { ReportsFormComponent, ResumeService } from '../../shared/components/reports-form/reports-form.component';
import { ReportService } from '../../services/report.service';

@Component({
    selector: 'app-management-event',
    imports: [CommonModule, RouterLink, ReactiveFormsModule, LoaderImage, ModalInfo, InfoEvent,
        HandleDatePipe, EventShareCardComponent, ModalShareEvent, RaffleNumbersComponent,
        QuestionaryComponent, TagPrize, InviteLoginComponent, ReportsFormComponent],
    templateUrl: './management-event.html',
    styleUrl: './management-event.css',
    providers: [HandleDatePipe]
})
export class ManagementEvent {
    @ViewChild('modalShareEvent') modalShareEvent!: ModalShareEvent;

    showInviteLogin: boolean = false;
    showFormReport: boolean = false;

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
    userLogged!: UserDTO;
    selectedEventId!: number;
    hasReport: boolean = false;         // flag para determinar si el usuario ya ha reportado el evento

    // TABS
    readonly TAB_INFO = 'info';
    readonly TAB_NUMBERS = 'numeros';
    readonly TAB_REGISTERED = 'registrados';
    tab: string = this.TAB_INFO;
    numeros: RaffleNumber[] = [];
    selectedRaffleNumbers: number[] = [];
    typesOfEventes = EventTypes;
    participants: RaffleParticipantDTO[] = [];
    eventType!: EventTypes;
    winners: WinnerDTO[] = [];
    accessRestricted: boolean = false;

    get canUserInscript(): boolean {
        if (!this.event) return false;
        const isCreator = this.authService.isAuthenticated() && (this.authService.getCurrentUserValue()?.id === this.event.creator?.id);
        const invite = this.route.snapshot.queryParamMap.get('invite');
        if (this.event.isPrivate && !isCreator && (!invite || invite.trim().length === 0)) {
            return false;
        }
        return !isCreator && !this.event?.isUserRegistered && this.event?.statusEvent === StatusEvent.OPEN;
    }

    constructor(
        private adminEventService: AdminEventService,
        private handleDatePipe: HandleDatePipe,
        private router: Router,
        private route: ActivatedRoute,
        private authService: AuthService,
        private eventService: EventsService,
        private cdr: ChangeDetectorRef,
        private notificationService: NotificationService,
        private adminInscriptService: AdminInscriptService,
        private reportsService: ReportService
    ) {}

    ngAfterViewInit(){
        this.adminEventService.winnersEvent$.subscribe(winners => {
            this.winners = winners;
        });
    }

    ngOnInit() {
        this.eventIdParam = Number(this.route.snapshot.paramMap.get('eventId'));
        // console.log("[admin-event] => ide del evento recibido por param: ", this.eventIdParam);
        // revisamos si los datos del evento ya fueron seteados desde la lista de eventos
        if (!this.event) {
            const invite = this.route.snapshot.queryParamMap.get('invite') || undefined;
            this.eventService.getEventById("" + this.eventIdParam, invite || undefined).subscribe({
                next: (resp) => {
                    this.adminEventService.setSelectedEvent(resp);
                },
                error: () => {
                    this.accessRestricted = true;
                    this.cdr.markForCheck();
                }
            })
        }
        // si hay un usuario logueado revisamos si ya ha reportado el evento
        this.adminEventService.selectedEvent$.subscribe(
            currentEvent => {
                this.eventAux = currentEvent ? { ...currentEvent } : null;
                this.event = currentEvent;
                if (this.event) {
                    this.eventType = this.event.eventType;
                    this.initForm();
                    this.cdr.markForCheck();

                    if(this.authService.isAuthenticated()){
                        this.reportsService.hasReportedEvent(""+this.event?.id, ""+this.authService.getCurrentUserValue()?.email).subscribe(
                            data => {
                                this.hasReport = data;
                                this.cdr.markForCheck();
                            },
                            error => {
                                console.log("[control-report] => Ha ocurrido un error al consultar los reportes del usuario");
                                this.hasReport = false;
                                this.cdr.markForCheck();
                            }
                        )
                    }
                }
            }
        )
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


    async onInscript(){
        if (this.accessRestricted) {
            return;
        }
        const respStatus = await this.adminInscriptService.checkStatusEventToInscript();
        // console.log("[onInscript] => estado del evento: ", respStatus);
        if(!respStatus){
            this.notificationService.notifyError("No fue posible realizar la operación");
        }
        else{
            if(respStatus != StatusEvent.OPEN){
                this.notificationService.notifyError("No fue posible realizar la operación. El evento se encuentra en estado: ", respStatus);
                if(this.event){
                    this.event.statusEvent = respStatus as StatusEvent;
                    this.cdr.detectChanges();
                }
            }
        }
    }

    goHome(){
        this.router.navigate(['/home']);
    }

    loadParticipants(eventId: number, eventType: EventTypes): void {
        this.eventService.getParticipantUsersByEventId(eventId, eventType, this.authService.getCurrentUserValue()?.email || "null").subscribe({
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
    getPlaceGoal(dataUser: RaffleParticipantDTO): any { // antes usaba userDTO
        // buscar el ganador por email en la lista WinnerDTO
        const dataPlace = this.winners.find(winner => winner.email === dataUser.email);
        if (!dataPlace)
            return { idUser: null, position: -1 };
        return { idUser: dataPlace.participantId, position: dataPlace.position };
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

    // le brinda al usuario la posibilidad de reportar un evento
    onReport(){
        // invitar al usuario a que se loguee para crear el reporte
        if(!this.authService.isAuthenticated()){
            this.showInviteLogin = true;
            this.cdr.detectChanges();
        }
        else{
            // mostramos el modal de reportes
            this.showFormReport = true;
            this.cdr.detectChanges();
        }
    }

    onReportClosed(){
        this.showFormReport = false;
    }

    onInviteClosed(data: any){
        this.showInviteLogin = false;
        this.cdr.detectChanges();
        if(data?.redirect){
            this.router.navigateByUrl("/login");
        }
    }

    // muestra el resultado del reporte del evento
    resultReport(data: ResumeService){
        // data.status == "OK" ? this.notificationService.notifySuccess(data.msg): this.notificationService.notifyError(data.msg);
        if(data.status == "OK"){
            this.notificationService.notifySuccess(data.msg);
            this.hasReport = true;
        }
        else{
            this.notificationService.notifyError(data.msg);
        }
    }

}
