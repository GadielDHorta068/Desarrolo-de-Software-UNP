import { Component, Input } from '@angular/core';
import { EventsTemp } from '../../../models/events.model';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { TagCategory } from '../tag-category/tag-category';

@Component({
  selector: 'app-info-event',
  imports: [CommonModule, HandleDatePipe, HandleTypePipe, HandleIconTypePipe, TagCategory],
  templateUrl: './info-event.html',
  styleUrl: './info-event.css'
})
export class InfoEvent {

  @Input() event!: EventsTemp|null;

}
