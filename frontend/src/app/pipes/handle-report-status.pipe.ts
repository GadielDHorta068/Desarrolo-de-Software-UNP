import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleReportStatusPipe'
})
export class HandleReportStatusPipe implements PipeTransform {

  transform( code: string|undefined ): string {
    let newStatus: string = "Desconocido";
    switch(code as string){
      case 'EARRING':
        newStatus = 'PENDIENTE';
        break;
      case 'APPROVED':
        newStatus = 'APROBADO';
        break;
      case 'REJECTED':
        newStatus = 'RECHAZADO';
    }

    return newStatus;
  }

}