import { Pipe, PipeTransform } from '@angular/core';
import { EventTypes } from '../models/events.model';
import { AuditActionType } from '../models/auditevent.model';

@Pipe({
  name: 'handleTypePipe'
})
export class HandleTypePipe implements PipeTransform {

  transform(value: string | EventTypes | AuditActionType | undefined): string {
    if (!value) return 'Desconocido';

    // Mapping para EventTypes
    if (Object.values(EventTypes).includes(value as EventTypes)) {
      switch(value as EventTypes) {
        case EventTypes.GIVEAWAY:
          return 'SORTEO';
        case EventTypes.RAFFLES:
          return 'RIFA';
        case EventTypes.CONTEST:
          return 'ADIVINANZAS';
        default:
          return 'Desconocido';
      }
    }

    // Mapping para AuditActionType
    const actionTypeMap: Record<string, string> = {
      'EVENT_CREATED': 'Evento Creado',
      'EVENT_UPDATED': 'Evento Actualizado',
      'EVENT_EXECUTED': 'Evento Ejecutado',
      'EVENT_CLOSED': 'Evento Cerrado',
      'EVENT_FINALIZED': 'Evento Finalizado',
      'USER_REGISTERED': 'Usuario Registrado',
      'USER_REGISTERED_FAILED': 'Registro de Usuario Fallido',
      'USER_UNREGISTERED': 'Usuario Dado de Baja',
      'NUMBER_PURCHASED': 'Número Comprado',
      'NUMBER_PURCHASED_FAILED': 'Compra de Número Fallida',
      'ERROR_OCURRED': 'Error Ocurrido',
      'SYSTEM_EVENT': 'Evento del Sistema'
    };

    return actionTypeMap[value as string] || 'Desconocido';
  }

}