import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EventsService } from '../../services/events.service';
import { ActivatedRoute } from '@angular/router';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { GuessprogressService } from '../../services/guessprogress.service';
import { QuestionaryComponent } from '../questionary/questionary.component';
import { UserDTO } from '../../models/UserDTO';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-guessprogress',
  standalone: true,
  imports: [CommonModule, FormsModule, QuestionaryComponent],
  templateUrl: './guessprogress.html',
  styleUrl: './guessprogress.css'
})
export class Guessprogress implements OnInit{

  @Output() closeGuessProgressModal = new EventEmitter<void>()
  @Output() proceedToInscript = new EventEmitter<string>();

  event!: EventsTemp | null;
  allEventStates = StatusEvent;
  
  //Estado del juego
  gamePhase: 'registration' | 'playing' | 'won' = 'registration';
  showModalInscript = false;

  //Variables del juego
  userGuess!: number;            // resultado ingresado de la entrada
  resultMessage: string = '';
  resultMessageType: 'info' | 'success' | 'error' = 'info';
  enteredNumbers: string = '';
  currentUser?: UserDTO;

  // Cronómetro
  elapsedTime: number = 0;
  private timerInterval?: any;
  readonly MAX_TIME_SECONDS = 3600; // 60 minutos

  //Intentos
  attemptCount: number = 0;
  maxAttempts: number = 0;

  //Rango
  minValue: number = 0;
  maxValue: number = 0;
  
  private subscription?: Subscription;         

  constructor(
    private eventService: EventsService,
    private activatedRoute: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private adminEventService: AdminEventService,
    private guessProgressService: GuessprogressService,
    private notificationService: NotificationService
  ){}

  ngOnInit(): void {
    // Obtener eventId de la ruta
    this.activatedRoute.params.subscribe(params => {
      const eventId = params['eventId'];
      console.log('eventId de ruta:', eventId); // Debug
      
      if (eventId) {
        // Obtener el evento del backend
        this.eventService.getEventById(eventId).subscribe({
          next: (event) => {
            console.log('Evento obtenido:', event); // Debug
            this.event = event;
            if (this.event && this.event?.statusEvent === this.allEventStates.OPEN) {
              this.initGuessProgress();
            }
          },
          error: (err) => {
            console.error('Error al obtener el evento:', err);
            this.notificationService.notifyError('Evento no encontrado');
            // Para testear sin backend, inicializa con datos dummy
            this.initGuessProgressDummy();
          }
        });
      } else {
        // Si no hay eventId, inicializa con datos dummy para testing
        this.initGuessProgressDummy();
      }
    });

    // Mantener el subscribe al adminEventService para cuando venga desde otra página
    this.subscription = this.adminEventService.selectedEvent$.subscribe(event => {
      if (event) {
        this.event = event;
        if (this.event && this.event?.statusEvent === this.allEventStates.OPEN) {
          this.initGuessProgress();
        }
      }
    });
  }

  private initGuessProgressDummy(): void {
    this.minValue = 1;
    this.maxValue = 100;
    this.maxAttempts = 10;
    this.gamePhase = 'registration';
    this.showModalInscript = true; // ← Esto es lo importante
    this.cdr.detectChanges();
  }

  private initGuessProgress(): void {
    if(!this.event) return;

    //Extraer datos del evento
    this.minValue = (this.event as any).minValue;
    this.maxValue = (this.event as any).maxValue;
    this.maxAttempts = (this.event as any).maxAttempts;

    // Resetear estado del juego
    this.gamePhase = 'registration';
    this.userGuess = undefined as any;
    this.resultMessage = '';
    this.enteredNumbers = '';
    this.attemptCount = 0;
    this.elapsedTime = 0;
    this.currentUser = undefined;
    this.showModalInscript = true;

    this.cdr.detectChanges();
  }

  onInscriptionSubmit(user: UserDTO): void {
    this.currentUser = user;
    
    //Verificar si el usuario ya participó
    this.guessProgressService.checkParticipation(this.event!.id, user.email).subscribe({
      next: (response) => {
        if(response.alreadyParticipated){
          this.notificationService.notifyError(response.message);
          this.closeModal();
          return;
        }

        // Usuario no ha participado, pasar a fase del juego
        this.gamePhase = 'playing';
        this.showModalInscript = false;
        this.startTimer();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error verificado participación: ', err);
        this.notificationService.notifyError('Error al verificar participación');
      }
    });
  //Debug sin backend
    /* this.gamePhase = 'playing';
        this.showModalInscript = false;
        this.startTimer();
        this.cdr.detectChanges(); */
  }

  onInscriptionClosed(): void{
    this.closeModal();
  }


  onGuessNumber(): void {
    if (!this.userGuess || this.userGuess === null) {
      this.resultMessage = 'Por favor ingresa un número';
      this.resultMessageType = 'error';
      this.cdr.detectChanges();
      return;
    }

    // Validar que esté dentro del rango
    if (this.userGuess < this.minValue || this.userGuess > this.maxValue) {
      this.resultMessage = `El número debe estar entre ${this.minValue} y ${this.maxValue}`;
      this.resultMessageType = 'error';
      this.cdr.detectChanges();
      return;
    }

    this.attemptCount++;

    // Verificar si ya excedió intentos
    if (this.attemptCount > this.maxAttempts) {
      this.resultMessage = `Has excedido el máximo de ${this.maxAttempts} intentos`;
      this.resultMessageType = 'error';
      this.gamePhase = 'won'; // Terminar juego (perdió)
      this.stopTimer();
      this.finishGame(false);
      this.cdr.detectChanges();
      return;
    }

    // Agregar número a la lista de intentos
    if (this.enteredNumbers) {
      this.enteredNumbers += `,${this.userGuess}`;
    } else {
      this.enteredNumbers = this.userGuess.toString();
    }

    // Llamar al backend para verificar el número
    this.guessProgressService.checkGuessNumber(this.event!.id, this.userGuess).subscribe({
      next: (response) => {
        this.resultMessage = response.message;
        this.resultMessageType = 'info';

        if (response.status === 'WIN') {
          this.resultMessageType = 'success';
          this.gamePhase = 'won';
          this.stopTimer();
          this.showWinAnimation();
          this.finishGame(true);
        }

        // Mostrar intentos restantes
        const remainingAttempts = this.maxAttempts - this.attemptCount;
        if (response.status !== 'WIN' && remainingAttempts > 0) {
          this.resultMessage += ` (Intentos restantes: ${remainingAttempts})`;
        }

        this.userGuess = undefined as any;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al verificar número:', err);
        this.resultMessage = 'Error al verificar el número';
        this.resultMessageType = 'error';
        this.cdr.detectChanges();
      }
    });
  }

  private startTimer(): void {
    this.timerInterval = setInterval(() => {
      if (this.elapsedTime < this.MAX_TIME_SECONDS) {
        this.elapsedTime++;
        this.cdr.detectChanges();
      } else {
        // Tiempo límite alcanzado
        this.stopTimer();
        this.resultMessage = 'Se acabó el tiempo (60 minutos)';
        this.resultMessageType = 'error';
        this.gamePhase = 'won';
        this.finishGame(false);
        this.cdr.detectChanges();
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = undefined;
    }
  }

  getFormattedTime(): string {
    const minutes = Math.floor(this.elapsedTime / 60);
    const seconds = this.elapsedTime % 60;
    return `${this.pad(minutes)}:${this.pad(seconds)}`;
  }

  private pad(num: number): string {
    return num < 10 ? `0${num}` : `${num}`;
  }

  private finishGame(hasWon: boolean): void {
    if (!this.currentUser || !this.event) return;

    // Preparar DTO para guardar el progreso
    const guessProgressDTO = {
      attemptCount: this.attemptCount,
      numbersTried: this.enteredNumbers,
      hasWon: hasWon,
      lastAttemptTime: new Date(),
      durationSeconds: this.elapsedTime
    };

    const participantRequestDTO = {
      user: this.currentUser,
      guessProgress: guessProgressDTO
    };

    // Registrar el resultado
    this.guessProgressService.registerEvent(this.event.id, participantRequestDTO).subscribe({
      next: (response) => {
        if (hasWon) {
          this.notificationService.notifySuccess('¡Felicidades! ¡Adivinaste el número!');
        } else {
          this.notificationService.notifyError('No adivinaste el número, pero tu participación fue registrada');
        }
      },
      error: (err) => {
        console.error('Error al registrar el juego:', err);
        this.notificationService.notifyError('Error al registrar tu participación');
      }
    });
  }

  showWinAnimation() {
    console.log('¡Usuario ganó!');
  }



  closeModal(): void {
    this.stopTimer();
    this.subscription?.unsubscribe();
    this.gamePhase = 'registration';
    this.closeGuessProgressModal.emit();
  }

  ngOnDestroy(): void {
    this.stopTimer();
    this.subscription?.unsubscribe();
  }
}
