import { Pipe, PipeTransform } from '@angular/core';
import { EventTypes, StatusEvent } from '../models/events.model';
import { AuditActionType } from '../models/auditevent.model';

@Pipe({
  name: 'handleTypePipe',
  standalone: true
})
export class HandleTypePipe implements PipeTransform {

  transform(value: string | EventTypes | AuditActionType | StatusEvent | undefined): string {
    if (!value) return 'Desconocido';

    const valueStr = String(value).toUpperCase();

    // Mapping para EventTypes/StatusEvent
    if (['GIVEAWAYS', 'RAFFLES', 'GUESSING_CONTEST', 'UNKNOWN', 'BLOCKED', 'CLOSED', 'OPEN', 'FINALIZED'].includes(valueStr)) {
      switch (valueStr) {
        case 'GIVEAWAYS':
          return 'SORTEO';
        case 'RAFFLES':
          return 'RIFA';
        case 'GUESSING_CONTEST':
          return 'ADIVINANZAS';
        case 'UNKNOWN':
          return 'Desconocido';
        case 'OPEN':
          return 'Abierto';
        case 'FINALIZED':
          return 'Finalizado';
        case 'BLOCKED':
          return 'Bloqueado';
        case 'CLOSED':
          return 'Cerrado';
        default:
          return 'Desconocido';
      }
    }

    
    
    // Mapping para AuditActionType
    const auditActions: Record<string, string> = {
      'EVENT_CREATED': 'Evento Creado',
      'EVENT_UPDATED': 'Evento Actualizado',
      'EVENT_EXECUTED': 'Evento Ejecutado',
      'EVENT_CLOSED': 'Evento Cerrado',
      'EVENT_FINALIZED': 'Evento Finalizado',
      'USER_REGISTERED': 'Usuario Registrado',
      'USER_REGISTERED_FAILED': 'Registro Fallido',
      'USER_UNREGISTERED': 'Usuario Dado de Baja',
      'NUMBER_PURCHASED': 'Número Comprado',
      'NUMBER_PURCHASED_FAILED': 'Compra Fallida',
      'ERROR_OCURRED': 'Error Ocurrido',
      'SYSTEM_EVENT': 'Evento del Sistema',
    };

    return auditActions[valueStr] || 'Desconocido';
  }

}