import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleDeliveryPipe'
})
export class HandleDeliveryPipe implements PipeTransform {

  transform(value: string|undefined): string {
    switch(value) {
      case "ANTES":
        return 'Antes de tiempo';
      case "DESPUES":
        return 'Luego de lo asentado';
      case "A_TIEMPO":
        return 'Como estaba asentado';
    }
    
    return '-';
  }

}