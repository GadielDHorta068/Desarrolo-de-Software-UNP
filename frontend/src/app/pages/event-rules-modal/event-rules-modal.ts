import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventsTemp, EventTypes } from '../../models/events.model';

@Component({
  selector: 'app-event-rules-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './event-rules-modal.html',
  styleUrl: './event-rules-modal.css',
})
export class EventRulesModal {

  isVisible = false;
  rules: string[] = [];
  allEventTypes = EventTypes;

  constructor() { }

  open(type: EventTypes | undefined): void {
    if (!type) {
      this.rules = ['No se ha especificado el tipo de evento.'];
    } else {
      this.rules = this.getRules(type);
    }
    this.isVisible = true;
  }

  close(): void {
    this.isVisible = false;
  }

  private getRules(type: EventTypes): string[] {
    switch (type) {
      case this.allEventTypes.GIVEAWAY:
        return [
          'Completar los campos requeridos del formulario.',
          'Presionar “Inscribirse”.',
          'El ganador será seleccionado de forma aleatoria.'
        ];
      case this.allEventTypes.RAFFLES:
        return [
          'Completar los campos requeridos del formulario.',
          'Seleccionar uno o más números disponibles.',
          'Presionar “Comprar” y completar los datos de pago.',
          'El ganador será elegido de manera aleatoria entre los números comprados.'
        ];
      case this.allEventTypes.GUESSING_CONTEST:
        return [
          'Completar el formulario inicial.',
          'Presionar “Participar” para ingresar al juego.',
          'Tendrá una cantidad determinada de intentos.',
          'Deberá adivinar un número dentro de un rango específico.',
          'El juego mostrará: intentos restantes, cronómetro, números ingresados y pistas de “cerca/lejos”.',
          'El ganador se determina por: si adivinó, cantidad de intentos utilizados, tiempo y momento de inscripción.'
        ];
      default:
        return ['Reglas no definidas para este tipo de evento.'];
    }
  }
}
