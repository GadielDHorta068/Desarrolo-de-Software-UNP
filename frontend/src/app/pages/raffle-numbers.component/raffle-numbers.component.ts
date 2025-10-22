import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { EventsTemp, EventTypes, RaffleNumber, StatusEvent,  } from '../../models/events.model';
import { EventsService } from '../../services/events.service';
import { QuestionaryComponent } from '../questionary/questionary.component';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-raffle-numbers',
  standalone: true,
  imports: [CommonModule, QuestionaryComponent],
  templateUrl: './raffle-numbers.component.html',
  styleUrl: './raffle-numbers.component.css'
})

export class RaffleNumbersComponent {

    @Output() closeRaffleNumberModal = new EventEmitter<void>()
    @Output() proceedToInscript = new EventEmitter<number[]>();

    eventIdParam!: Number;
    event!: EventsTemp | null;
    allEventTypes = EventTypes;
    actualEventType!: EventTypes;

    numeros: RaffleNumber[] = [];
    selectedNumbers: number[] = [];

    // Variables de los modales
    allEventStates = StatusEvent;
    showModalIncript = false;

    private subscription?: Subscription;

    constructor (
        private eventService: EventsService,
        private activatedRoute: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) {

    }


    ngOnInit() {
        this.eventIdParam = Number(this.activatedRoute.snapshot.paramMap.get('eventId'));
        if (this.eventIdParam != null) {
            this.eventService.getEventById("" + this.eventIdParam).subscribe(
                resp => {
                    this.event = resp;
                    if (this.event && this.event.statusEvent === this.allEventStates.OPEN) {
                        this.initRaffleNumbers();
                    }
                    // HACER ALGO SI NO ESTA ABIERTO EL EVENTO

                }
            );
        }
    }

    // onModalClosed(): void {
    //     this.showModalIncript = false; // oculta el modal
    //     this.subscription?.unsubscribe(); // Elimina todas las subscripciones????
    //     setTimeout(() => this.initRaffleNumbers(), 500); // refresca los números
    // }
    
    onConfirmSelection(): void {
        this.proceedToInscript.emit(this.selectedNumbers);
        this.closeRaffleNumberModal.emit()
    }
    
    closeModal(): void {
        this.subscription?.unsubscribe(); // Elimina todas las subscripciones????
        this.closeRaffleNumberModal.emit();
    }

    // selectNumber(aRaffleNumber: RaffleNumber): void {
    //     if (!aRaffleNumber.buyStatus) {
    //         aRaffleNumber.selectStatus = !aRaffleNumber.selectStatus;
    //     }
    // }

    selectNumber(aRaffleNumber: RaffleNumber): void {
        if (!aRaffleNumber.buyStatus) {
            aRaffleNumber.selectStatus = !aRaffleNumber.selectStatus;

            if (aRaffleNumber.selectStatus) {
                // Si se seleccionó, agregamos el número
                this.selectedNumbers.push(aRaffleNumber.ticketNumber);
            } else {
                // Si se deseleccionó, lo removemos
                this.selectedNumbers = this.selectedNumbers.filter(n => n !== aRaffleNumber.ticketNumber);
            }
        }
        console.log('Numeros en el modal' + this.selectNumber);
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

    // purchaseNumbers(): void {
    //     if (this.event?.statusEvent === this.allEventStates.OPEN) {
    //         const seleccionados = this.numeros.filter(n => n.selectStatus && !n.buyStatus);
    //         this.selectedNumbers = seleccionados.map(n => n.ticketNumber); // guardamos los números
            
    //         this.showModalIncript = true; // muestra el modal de Questionary

    //         // this.selectedEventId = this.event.id;
    //         // this.actualEventType = this.event.eventType;
    //     }
    // }
}
