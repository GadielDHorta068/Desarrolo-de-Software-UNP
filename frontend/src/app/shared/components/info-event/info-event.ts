import { Component, Input } from '@angular/core';
import { EventsTemp, EventTypes, RaffleEvent } from '../../../models/events.model';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { TagCategory } from '../tag-category/tag-category';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';

@Component({
  selector: 'app-info-event',
  imports: [CommonModule, HandleDatePipe, HandleTypePipe, HandleIconTypePipe, HandleStatusPipe, TagCategory],
  templateUrl: './info-event.html',
  styleUrl: './info-event.css'
})
export class InfoEvent {

  eventTypes = EventTypes;

  @Input() event!: EventsTemp|null;
  // @Input() event!: RaffleEvent|null;

}
