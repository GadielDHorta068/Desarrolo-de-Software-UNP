import { CommonModule } from '@angular/common';
import { Component, signal, computed, ViewChildren, ElementRef, QueryList } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EventsService } from '../../services/events.service';
import { NotificationService } from '../../services/notification.service';
import confetti from 'canvas-confetti';
import { WinnerDTO } from '../../models/winner.model';
import { ParticipantDTO } from '../../models/participant.model';
import { EventsTemp, EventTypes } from '../../models/events.model';

@Component({
  selector: 'app-winners-wheel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './winners-wheel.html',
  styleUrl: './winners-wheel.css'
})
export class WinnersWheel {
  @ViewChildren('reelElements') reelElements!: QueryList<ElementRef>;

  eventId!: number;
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  event = signal<EventsTemp | null>(null);
  participants = signal<ParticipantDTO[]>([]);
  winners = signal<WinnerDTO[]>([]);
  reels = signal<{ participants: ParticipantDTO[] }[]>([]);
  isSpinning = signal(false);

  // Fases: loading → jackpot
  phase = signal<'loading' | 'jackpot'>('loading');

  // Computed para determinar si es un evento tipo RAFFLE
  isRaffleEvent = computed(() => this.event()?.eventType === EventTypes.RAFFLES);

  // Método para obtener el texto a mostrar (nombre o número de rifa + nombre)
  getDisplayText = (item: WinnerDTO | ParticipantDTO): string => {
    if (this.isRaffleEvent() && 'raffleNumber' in item && item.raffleNumber !== undefined) {
      return `Número ${item.raffleNumber} - ${item.name} ${item.surname}`;
    }
    return `${item.name} ${item.surname}`;
  };

  constructor(
    private route: ActivatedRoute,
    private eventsService: EventsService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.eventId = Number(this.route.snapshot.paramMap.get('eventId'));
    if (!this.eventId || isNaN(this.eventId)) {
      this.errorMessage.set('Evento inválido');
      this.loading.set(false);
      return;
    }
    this.fetchData();
  }

  private fetchData() {
    this.loading.set(true);
    this.phase.set('loading');

    const event$ = this.eventsService.getEventById(this.eventId.toString()).pipe(
      catchError(err => {
        this.handleError(err, 'Error al obtener información del evento');
        return of(null);
      })
    );

    const winners$ = this.eventsService.getWinnersByEventId(this.eventId).pipe(
      catchError(err => {
        this.handleError(err, 'Error al obtener ganadores');
        return of([]); // Devolver un array vacío en caso de error
      })
    );

    // Primero obtenemos el evento para determinar su tipo
    event$.subscribe(event => {
      this.event.set(event);
      
      // Solo obtener participantes si NO es un evento tipo raffle
      const participants$ = event?.eventType === EventTypes.RAFFLES 
        ? of([]) // Para rifas, no hay participantes, solo números
        : this.eventsService.getParticipantsByEventId(this.eventId).pipe(
            catchError(err => {
              this.handleError(err, 'Error al obtener participantes');
              return of([]);
            })
          );

      forkJoin({
        participants: participants$,
        winners: winners$
      }).subscribe(({ participants, winners }) => {
        this.participants.set(participants || []);
        const sortedWinners = (winners || []).sort((a, b) => a.position - b.position);
        this.winners.set(sortedWinners);
        this.loading.set(false);

        if (this.winners().length > 0) {
          this.buildReels();
          this.phase.set('jackpot');
          // Iniciar la animación automáticamente
          setTimeout(() => this.startJackpot(), 100);
        } else if (!this.errorMessage()) {
          this.errorMessage.set('No se encontraron ganadores para este evento.');
        }
      });
    });
  }

  private buildReels() {
    const winners = this.winners();
    const participants = this.participants();
    const reels = winners.map(winner => {
      // Crear una lista de participantes para el carrete, asegurando que el ganador esté
      let reelParticipants = [winner, ...participants.filter(p => p.participantId !== winner.participantId)];
      // Mezclar aleatoriamente para la animación
      reelParticipants = reelParticipants.sort(() => Math.random() - 0.5);
      // Asegurarse de que el ganador esté en la lista para encontrar su índice
      const winnerIndex = reelParticipants.findIndex(p => p.participantId === winner.participantId);
      if (winnerIndex === -1) reelParticipants.unshift(winner);
      
      // Clonar y añadir participantes para un bucle infinito en la animación
      const extendedParticipants = [...reelParticipants, ...reelParticipants, ...reelParticipants];

      return { participants: extendedParticipants };
    });
    this.reels.set(reels);
  }

  startJackpot() {
    if (this.phase() !== 'jackpot' || this.isSpinning()) return;
    this.isSpinning.set(true);

    const reelElements = this.reelElements.toArray();
    this.winners().forEach((winner, i) => {
      const reelEl = reelElements[i].nativeElement as HTMLElement;
      const reel = this.reels()[i];
      const winnerIndex = reel.participants.findIndex(p => p.participantId === winner.participantId);

      // Iniciar animación de giro
      reelEl.classList.add('spinning');

      setTimeout(() => {
        // Detener el giro
        reelEl.classList.remove('spinning');
        
        // Calcular la posición final para centrar al ganador
        const targetPosition = -winnerIndex * 100; // 100 es la altura del item
        reelEl.style.transform = `translateY(${targetPosition}px)`;

        // Efecto de confeti para cada ganador
        confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });

        // Si es el último carrete, el sorteo ha finalizado
        if (i === this.winners().length - 1) {
          this.notificationService.notifySuccess('¡Sorteo finalizado!');
        }
      }, (i + 1) * 1000 + 4000); // 4s de giro + 1s de retraso entre cada carrete
    });
  }

  private handleError(err: any, fallback: string) {
    this.loading.set(false);
    const msg = err?.error || err?.error?.message || err?.message || fallback;
    this.errorMessage.set(msg);
    this.notificationService.notifyError(msg);
  }
}
