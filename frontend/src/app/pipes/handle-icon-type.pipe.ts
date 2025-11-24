import { Pipe, PipeTransform } from '@angular/core';
import { EventTypes } from '../models/events.model';

@Pipe({
  name: 'handleIconTypePipe'
})
export class HandleIconTypePipe implements PipeTransform {

  transform(code: string | undefined): string {
    let srcImage: string = "";
    switch (code as string) {
      case EventTypes.GIVEAWAY:
        srcImage = 'assets/roulette_icon.png';
        break;
      case EventTypes.RAFFLES:
        srcImage = 'assets/raffle_icon.png';
        break;
      case EventTypes.GUESSING_CONTEST:
        srcImage = 'assets/guessing_icon.png';
        break;
      default:
        srcImage = 'assets/raffle_icon.png';
        break;
    }

    return srcImage;
  }

}