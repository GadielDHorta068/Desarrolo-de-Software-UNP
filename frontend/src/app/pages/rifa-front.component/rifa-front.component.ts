import { Component } from '@angular/core';
import { RaffleNumber } from './RaffleNumber';
import { CommonModule } from '@angular/common';
import { EventsTemp, EventTypes } from '../../models/events.model';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsService } from '../../services/events.service';

@Component({
  selector: 'app-rifa-front.component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rifa-front.component.html',
  styleUrl: './rifa-front.component.css'
})

export class RifaFrontComponent {

    tab: 'info' | 'numeros' | 'registrados' = 'info';
    
    public raffle!: EventsTemp | null;
    
    
    constructor (
        private eventService: EventsService,
        private adminEventService: AdminEventService
    ) {
        this.adminEventService.selectedEvent$.subscribe(
            currentEvent => {
                // this.eventAux = currentEvent ? {...currentEvent}: null;
                this.raffle = currentEvent;
                console.log("[edicion] => evento seleccionado: ", this.raffle);
                // this.updateForm();
            }
        )
    }

    numeros: RaffleNumber[] = [];

    ngOnInit() :void {
        
        if (this.raffle && this.raffle.eventType === EventTypes.RAFFLES) {
            const total = this.raffle?.quantityOfNumbers;
            
            this.eventService.getSoldNumbersByRaffleId(this.raffle?.id).subscribe({
                next: (boughtNumbers: number[]) => {
                    this.numeros = Array.from({ length: total }, (_, i) => ({
                        ticketNumber: i + 1,
                        buyStatus: boughtNumbers.includes(i + 1), // ejemplo
                        selectStatus: false
                    }));
                },
                error: (err) => {
                    console.error('Error al obtener los números vendidos: ', err);
                }
            });
        }

    }

    selectNumber(aRaffleNumber: RaffleNumber) :void {
        if(!aRaffleNumber.buyStatus) {
            aRaffleNumber.selectStatus = !aRaffleNumber.selectStatus; 
        }
    }

    addToCart() : void {
        const seleccionados = this.numeros.filter(n => n.selectStatus && !n.buyStatus);
        console.log('Números seleccionados:', seleccionados.map(n => n.ticketNumber));
        alert('Seleccionados: ' + seleccionados.map(n => n.ticketNumber).join(', '));
    }
}
