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
    if(Object.values(AuditActionType).includes(value as AuditActionType)) {
        switch(value as AuditActionType) {
          case AuditActionType.EVENT_CREATED:
            return 'Evento Creado';
          case AuditActionType.EVENT_UPDATED:
            return 'Evento Actualizado';
          case AuditActionType.EVENT_EXECUTED:
            return 'Evento Ejecutado';
          case AuditActionType.EVENT_CLOSED:
            return 'Evento Cerrado';
          case AuditActionType.EVENT_FINALIZED:
            return 'Evento Finalizado';
          case AuditActionType.USER_REGISTERED:
            return 'Usuario Registrado';
          case AuditActionType.USER_REGISTERED_FAILED:
            return 'Registro de Usuario Fallido';
          case AuditActionType.USER_UNREGISTERED:
            return 'Usuario Dado de Baja';
          case AuditActionType.NUMBER_PURCHASED:
            return 'Número Comprado';
          case AuditActionType.NUMBER_PURCHASED_FAILED:
            return 'Compra de Número Fallida';
          case AuditActionType.ERROR_OCURRED:
            return 'Error Ocurrido';
          case AuditActionType.SYSTEM_EVENT:
            return 'Evento del Sistema';
          default:
            return 'Desconocido'
      }
    } 
    return 'Desconocido';    
  }

}