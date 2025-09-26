import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleStatusPipe'
})
export class HandleStatusPipe implements PipeTransform {

  transform( code: string|undefined ): string {
    let newStatus: string = "Desconocido";
    switch(code as string){
      case 'OPEN':
        newStatus = 'ABIERTO';
        break;
      case 'ACTIVE':
        newStatus = 'ACTIVO';
        break;
      case 'CLOSED':
        newStatus = 'CERRADO';
        break;
      case 'BLOCKED' :
        newStatus = 'BLOQUEADO';
        break;
      case 'FINISHED' :
        newStatus = 'FINALIZADO';
    }

    return newStatus;
  }

}