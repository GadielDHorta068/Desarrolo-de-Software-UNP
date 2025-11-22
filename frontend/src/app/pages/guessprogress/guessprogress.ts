import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EventsService } from '../../services/events.service';
import { ActivatedRoute } from '@angular/router';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { GuessprogressService } from '../../services/guessprogress.service';

@Component({
  selector: 'app-guessprogress',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './guessprogress.html',
  styleUrl: './guessprogress.css'
})
export class Guessprogress implements OnInit{

  @Output() closeGuessProgressModal = new EventEmitter<void>()
  @Output() proceedToInscript = new EventEmitter<string>();

  eventIdParam!: Number;
  event!: EventsTemp | null;
  allEventTypes = EventTypes;
  actualEventType!: EventTypes;

  
  allEventStates = StatusEvent;
  showModalIncript = false;
  private subscription?: Subscription

  userGuess!: number;            // resultado ingresado de la entrada
  resultMessage: any;
  enteredNumbers: string = "";  // aca se guarda los número (ej: 1,2,3)
  contestId!: number;          

  constructor(
    private eventService: EventsService,
    private activatedRoute: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private adminEventService: AdminEventService,
    private guessProgressService: GuessprogressService
  ){}

  ngOnInit(): void {
    this.adminEventService.selectedEvent$.subscribe(
      event => {
        this.event = event;
        if(this.event && this.event?.statusEvent == this.allEventStates.OPEN){
          this.initGuessProgress();
        }
        // HACER ALGO SI NO ESTA ABIERTO EL EVENTO
      }
    )
  }

  closeModal(): void {
    this.subscription?.unsubscribe();
    this.closeGuessProgressModal.emit();
  }

  private initGuessProgress(): void {

  }

  onGuessNumber(): void {
  const guessed = this.userGuess; // lo que escribió el usuario

  this.guessProgressService.checkGuessNumber(this.event!.id, guessed)
    .subscribe({
      next: (response) => {
        // response.status = HIGHER | LOWER | WIN
        // response.message = "El número objetivo es mayor." etc.
        
        console.log("Respuesta del backend: ", response);

        if (response.status === 'WIN') {
          this.showWinAnimation(); 
        }

        // Mostrar el mensaje en pantalla
        this.resultMessage = response.message;
      },
      error: (err) => {
        console.error("Error en la llamada al back", err);
      }
    });
}
  showWinAnimation() {
    throw new Error('Method not implemented.');
  }


}
