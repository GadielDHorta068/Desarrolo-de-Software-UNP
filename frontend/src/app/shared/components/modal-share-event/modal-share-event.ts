import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { EventShareCardComponent } from '../../event-share-card/event-share-card.component';

@Component({
  selector: 'app-modal-share-event',
  imports: [CommonModule, EventShareCardComponent],
  templateUrl: './modal-share-event.html',
  styleUrl: './modal-share-event.css'
})
export class ModalShareEvent {

  show: boolean = false;
  @Input() eventId?: number;

  constructor(
    private cdr: ChangeDetectorRef
  ){}

  open() {
    // console.log("[openModalInfo] => evento recibido: ", this.dataModal);
    this.show = true;
    this.cdr.detectChanges();
  }

  close() {
    this.show = false;
    this.cdr.detectChanges();
  }

}
