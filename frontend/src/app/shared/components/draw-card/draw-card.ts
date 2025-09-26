import { Component, input, Input } from '@angular/core';
import { Events, EventsTemp } from '../../../models/events.model';
import { CommonModule } from '@angular/common';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { ModalDrawInfo } from '../modal-draw-info/modal-draw-info';
import { Router } from '@angular/router';
import { AdminEventService } from '../../../services/admin/adminEvent.service';
import { QuestionaryComponent } from '../../../pages/questionary/questionary.component';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule, HandleStatusPipe, HandleIconTypePipe, HandleDatePipe, ModalDrawInfo, QuestionaryComponent],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard {

  // @Input() event!: Events|null;
  @Input() event!: EventsTemp|null;
  customBackground = input<string>('bg-white');

  constructor(
    private router: Router,
    private adminEventService: AdminEventService
  ){}

  // PRUEBA QUESTIONARY MODAL
    showModal = false; // el modal empieza desactivado
    selectedEventId!: number;

    openModal(aEventId?: number) {
        if (!aEventId) {
        console.warn("eventId inválido:", aEventId); //borrar
        return;
        }
        this.selectedEventId = aEventId;
        this.showModal = true;
    }
  // PRUEBA QUESTIONARY MODAL

  public redirectEdit() {
    this.adminEventService.setSelectedEvent(this.event);
    this.router.navigate(['/event-edit']);
  }

  public onIncript(){
    console.log("Presiona incribirse!");
    alert("Se apreto INCRIBIRME");
  }
  
}
