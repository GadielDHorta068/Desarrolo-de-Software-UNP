import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { EventsTemp, EventTypes, RaffleEvent, StatusEvent } from '../../../models/events.model';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { TagCategory } from '../tag-category/tag-category';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { WinnerDTO } from '../../../models/winner.model';
import { TagPrize } from '../tag-prize/tag-prize';

@Component({
  selector: 'app-info-event',
  imports: [CommonModule, HandleDatePipe, HandleTypePipe, HandleIconTypePipe, HandleStatusPipe, TagCategory, TagPrize],
  templateUrl: './info-event.html',
  styleUrl: './info-event.css'
})
export class InfoEvent implements OnInit, OnChanges{
  
  eventTypes = EventTypes;
  eventStatus = StatusEvent;

  @Input() event!: EventsTemp|null;
  // @Input() event!: RaffleEvent|null;
  @Input() winners: WinnerDTO[] = [];

  ngOnInit(): void {
    console.log("[infoEvent] => ganadores recibidos: ", this.winners);
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log("[infoEvent] => cambios detectados: ", changes);
    // if (changes['valor']) {
    //   const prev = changes['valor'].previousValue;
    //   const curr = changes['valor'].currentValue;
    //   console.log(`Cambio detectado: de "${prev}" a "${curr}"`);
    // }
  }

}
