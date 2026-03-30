import { Pipe, PipeTransform } from '@angular/core';
import { StatusReport } from '../services/report.service';

@Pipe({
  name: 'handleReportStatusPipe',
  standalone: true
})
export class HandleReportStatusPipe implements PipeTransform {

  transform( code: StatusReport |string|undefined ): string {
    if(!code) return "Desconocido";
    const codeStr = String(code).toUpperCase();
    switch(codeStr){
      case 'EARRING':
        return 'PENDIENTE';
        break;
      case 'APPROVED':
        return 'APROBAR';
        break;
      case 'REJECTED':
        return 'DESESTIMAR';
      default:
        return 'Desconocido';
    }
  }

}