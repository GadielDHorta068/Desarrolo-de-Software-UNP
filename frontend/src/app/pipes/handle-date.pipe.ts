import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleDatePipe'
})
export class HandleDatePipe implements PipeTransform {

  transform( arrDate: number[]|undefined): string {
    let date: string = "-";
    if(arrDate && (arrDate.length == 3)){
      date = arrDate[2] +"-"+arrDate[1] +"-"+ arrDate[0];
    }
    return date;
  }

}