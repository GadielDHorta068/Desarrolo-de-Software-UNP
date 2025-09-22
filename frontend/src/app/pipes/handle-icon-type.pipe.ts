import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleIconTypePipe'
})
export class HandleIconTypePipe implements PipeTransform {

  transform( code: string|undefined ): string {
    let srcImage: string = "assets/raffle_icon.png";
    // switch(code as string){
    //   case 'GIVEAWAY':
    //     srcImage = 'assets/sorteo_2.png';
    //     break;
    //   case 'CONTEST':
    //     srcImage = 'assets/sorteo_3.png';
    //     break;
    //   case 'TOURNAMENT' :
    //     srcImage = 'assets/sorteo_4.png';
    // }

    return srcImage;
  }

}