import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Events, EventsTemp } from '../../../models/events.model';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { TagCategory } from '../tag-category/tag-category';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';

@Component({
  selector: 'app-modal-draw-info',
  imports: [CommonModule, HandleIconTypePipe, HandleDatePipe, TagCategory],
  templateUrl: './modal-draw-info.html',
  styleUrl: './modal-draw-info.css'
})
export class ModalDrawInfo {
  show: boolean = false;

  // @Input() dataEvent!: Events|null;
  @Input() dataEvent!: EventsTemp|null;

  open() {
    console.log("[openModal] => evento recibido: ", this.dataEvent);
    this.show = true;
  }

  close() {
    this.show = false;
  }
  
}
