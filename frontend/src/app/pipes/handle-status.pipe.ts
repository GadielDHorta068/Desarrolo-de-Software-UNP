import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleStatusPipe'
})
export class HandleStatusPipe implements PipeTransform {

  transform( code: string|undefined ): string {
    let newStatus: string = "Desconocido";
    switch(code as string){
      case 'open':
        newStatus = 'ABIERTO';
        break;
      case 'active':
        newStatus = 'ACTIVO';
        break;
      case 'closed':
        newStatus = 'CERRADO';
        break;
      case 'blocked' :
        newStatus = 'BLOQUEADO';
        break;
      case 'finished' :
        newStatus = 'FINALIZADO';
    }

    return newStatus;
  }

}