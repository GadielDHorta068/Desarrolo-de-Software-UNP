import { Pipe, PipeTransform } from '@angular/core';
import { EventTypes } from '../models/events.model';

@Pipe({
  name: 'handleTypePipe'
})
export class HandleTypePipe implements PipeTransform {

  transform( eventType: string|undefined ): string {
    let type: string = "Desconocido";
    switch(eventType as string){
      case EventTypes.GIVEAWAY:
        type = 'SORTEO';
        break;
      case EventTypes.RAFFLES:
        type = 'RIFA';
        break;
      case EventTypes.CONTEST:
        type = 'ADIVINANZAS';
        break;
    }

    return type;
  }

}