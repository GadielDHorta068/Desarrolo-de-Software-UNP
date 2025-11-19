import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { EventsTemp, EventTypes, RaffleNumber, StatusEvent,  } from '../../models/events.model';
import { EventsService } from '../../services/events.service';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { PaginationComponent } from '../pagination.component/pagination.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-raffle-numbers',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
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

    // paginacion
    pageSize = 60;
    currentPage = 1;

    // search
    searchTerm: string = '';

    // Variables de los modales
    allEventStates = StatusEvent;
    showModalIncript = false;

    private subscription?: Subscription;

    constructor (
        private eventService: EventsService,
        private activatedRoute: ActivatedRoute,
        private cdr: ChangeDetectorRef,
        private adminEventService: AdminEventService
    ) {

    }

    // by A.T
    ngOnInit() {
        this.adminEventService.selectedEvent$.subscribe(
            event => {
                this.event = event;
                if (this.event && this.event.statusEvent === this.allEventStates.OPEN) {
                        this.initRaffleNumbers();
                    }
                    // HACER ALGO SI NO ESTA ABIERTO EL EVENTO
            }
        )
    }
    
    onConfirmSelection(): void {
        this.proceedToInscript.emit(this.selectedNumbers);
        this.closeRaffleNumberModal.emit()
    }
    
    closeModal(): void {
        this.subscription?.unsubscribe(); // Elimina todas las subscripciones????
        this.closeRaffleNumberModal.emit();
    }

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
        // console.log("[selectNumber] => nros elegidos: ",this.selectedNumbers);
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

    // ---------------------------
    // METODOS PARA LA BUSQUEDA
    // ---------------------------
    get filteredNumbers(): RaffleNumber[] {
        const term = this.searchTerm.trim();
        if(!term) {
            return this.numeros;
        }
        let filtered = this.numeros.filter(n => !n.buyStatus);

        return this.numeros.filter(n => !n.buyStatus && n.ticketNumber.toString().endsWith(term));
    }

    // ---------------------------
    // METODOS PARA LA PAGINACION
    // ---------------------------
    get totalPages(): number {
        return Math.ceil(this.filteredNumbers.length / this.pageSize);
    }

    // este metodo recorta el array original para obtener los indicies
    // de el primer y ultimo numero  q se mostraran en la pagina
    // esto incluye los q se encuentran en el medio
    get paginatedNumbers(): RaffleNumber[] {
        const start = (this.currentPage - 1) * this.pageSize;
        return this.filteredNumbers.slice(start, start + this.pageSize);
    }

    onPageChange(aPageNumber: number): void {
        this.currentPage = aPageNumber;
    }

}
