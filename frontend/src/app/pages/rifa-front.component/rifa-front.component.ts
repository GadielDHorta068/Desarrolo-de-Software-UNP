import { Component } from '@angular/core';
import { RaffleNumber } from './RaffleNumber';
import { CommonModule } from '@angular/common';
import { RaffleEvent } from '../../models/events.model';

@Component({
  selector: 'app-rifa-front.component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rifa-front.component.html',
  styleUrl: './rifa-front.component.css'
})

export class RifaFrontComponent {

    constructor () {}

    tab: 'info' | 'numeros' | 'registrados' = 'info';

    private raffle!: RaffleEvent;

    
    // MOCK RIFA
    rifa = {
        titulo: 'Rifa de Camisetas Deportivas',
        descripcion: 'Participá por increíbles camisetas deportivas de tu equipo favorito.',
        precio: 1000,
        etiquetas: ['Ropa', 'Deporte']
    };

    numeros: RaffleNumber[] = [];

    ngOnInit() :void {
        const total = 100;

        // En el futuro esto vendrá del backend (por ahora mock)
        this.numeros = Array.from({ length: total }, (_, i) => ({
            ticketNumber: i + 1,
            buyStatus: [3, 15, 27].includes(i + 1), // ejemplo
            selectStatus: false
        }));
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
