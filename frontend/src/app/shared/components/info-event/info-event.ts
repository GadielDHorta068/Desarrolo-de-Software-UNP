import { Component, Input } from '@angular/core';
import { EventsTemp } from '../../../models/events.model';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-info-event',
  imports: [CommonModule, HandleDatePipe],
  templateUrl: './info-event.html',
  styleUrl: './info-event.css'
})
export class InfoEvent {

  @Input() event!: EventsTemp|null;

}
