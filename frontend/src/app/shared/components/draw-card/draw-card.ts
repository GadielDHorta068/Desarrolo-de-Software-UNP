import { AfterViewInit, ChangeDetectorRef, Component, input, Input } from '@angular/core';
import { Events, EventsTemp } from '../../../models/events.model';
import { CommonModule } from '@angular/common';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { ModalDrawInfo } from '../modal-draw-info/modal-draw-info';
import { Router } from '@angular/router';
import { AdminEventService } from '../../../services/admin/adminEvent.service';
import { QuestionaryComponent } from '../../../pages/questionary/questionary.component';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { AuthService, UserResponse } from '../../../services/auth.service';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule, HandleStatusPipe, HandleIconTypePipe, HandleDatePipe, ModalDrawInfo, QuestionaryComponent],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard implements AfterViewInit{

  userCurrent: UserResponse|null = null;
  // @Input() event!: Events|null;
  @Input() event!: EventsTemp|null;
  customBackground = input<string>('bg-white');

  isCreator: boolean = false;

  constructor(
    private router: Router,
    private adminEventService: AdminEventService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ){
    this.userCurrent = this.authService.getCurrentUserValue();
    console.log("[card-event] => usuario actual: ", this.userCurrent);
  }

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

  ngAfterViewInit(){
    this.reviewCreator();
  }

  public redirectEdit() {
    this.adminEventService.setSelectedEvent(this.event);
    this.router.navigate(['/event-edit']);
  }

  public onIncript(){
    console.log("Presiona incribirse!");
    alert("Se apreto INCRIBIRME");
  }

  private reviewCreator(){
    // console.log("[card-event] => datos del usuario: ", this.userCurrent);
    // console.log("[card-event] => datos del evento: ", this.event);
    // console.log("[card-event] => es el creador: ", (this.userCurrent?.id == this.event?.creator.id));
    
    if( this.userCurrent?.id && this.event?.creator.id && (this.userCurrent?.id == this.event?.creator.id)){
      this.isCreator = true;
    }
    else{
      this.isCreator = false;
    }
    this.cdr.detectChanges();
  }
  
}
