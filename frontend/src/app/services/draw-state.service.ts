import { Injectable } from '@angular/core';
import { signal, computed } from '@angular/core';
import { EventsService } from '../services/events.service';
import { NotificationService } from '../services/notification.service';
import { ParticipantDTO } from '../models/participant.model';
import { WinnerDTO } from '../models/winner.model';

export type DrawPhase = 'idle' | 'spinning' | 'highlighting' | 'podium';

@Injectable({ providedIn: 'root' })
export class DrawStateService {
  readonly participants = signal<ParticipantDTO[]>([]);
  readonly winners = signal<WinnerDTO[]>([]);
  readonly loading = signal(false);
  readonly phase = signal<DrawPhase>('idle');
  readonly revealStep = signal(0);
  readonly highlightIndex = signal<number>(-1);
  readonly showPodium = signal(false);
  readonly errorMessage = signal<string | null>(null);

  // Derived state
  readonly mainWinner = computed(() => {
    const ws = this.winners();
    return ws.find(w => w.position === 1) || ws[0] || null;
  });

  readonly podium = computed(() => {
    return this.winners()
      .filter(w => w.position <= 3)
      .sort((a, b) => a.position - b.position);
  });

  private highlightTimer: any = null;

  constructor(
    private eventsService: EventsService,
    private notificationService: NotificationService
  ) {}

  reset(): void {
    this.loading.set(false);
    this.phase.set('idle');
    this.revealStep.set(0);
    this.highlightIndex.set(-1);
    this.showPodium.set(false);
    this.errorMessage.set(null);
    if (this.highlightTimer) {
      clearInterval(this.highlightTimer);
      this.highlightTimer = null;
    }
  }

  setParticipants(list: ParticipantDTO[]): void {
    this.participants.set(list || []);
  }

  loadEvent(eventId: number): void {
    this.reset();
    if (!eventId || isNaN(eventId)) {
      this.errorMessage.set('Evento inválido');
      return;
    }
    this.loading.set(true);
    this.phase.set('spinning');
    this.eventsService.getParticipantsByEventId(eventId).subscribe({
      next: (resp: ParticipantDTO[]) => {
        this.participants.set(resp || []);
        this.loading.set(false);
        this.phase.set('idle');
        this.notificationService.notifySuccess('Participantes cargados');
      },
      error: (err) => {
        const msg = err?.error?.message || err?.message || 'Error al obtener participantes';
        this.errorMessage.set(msg);
        this.loading.set(false);
        this.phase.set('idle');
        this.notificationService.notifyError(msg);
      }
    });
  }

  startDraw(): void {
    const list = this.participants();
    if (!list.length) {
      this.notificationService.notifyError('No hay participantes para el sorteo');
      return;
    }

    this.loading.set(false);
    this.phase.set('highlighting');
    this.revealStep.set(1);

    let count = 0;
    const maxShows = Math.min(list.length * 2, 50);
    this.highlightTimer = setInterval(() => {
      const rnd = Math.floor(Math.random() * list.length);
      this.highlightIndex.set(rnd);
      count++;
      if (count >= maxShows) {
        clearInterval(this.highlightTimer);
        this.highlightTimer = null;
        // Detener en el ganador
        const winnerIdx = Math.floor(Math.random() * list.length);
        this.highlightIndex.set(winnerIdx);
        const chosen = list[winnerIdx];
        this.revealStep.set(2);
        // Construir podio simple: ganador + dos nombres aleatorios si existen
        const others = this.pickTwoOthers(list, winnerIdx);
        const podium: WinnerDTO[] = [
          { participantId: chosen.participantId, name: chosen.name, surname: chosen.surname, position: 1, email: undefined as any, phone: undefined as any, eventId: chosen.eventId, eventTitle: chosen.eventTitle },
          ...others.map((p, i) => ({ participantId: p.participantId, name: p.name, surname: p.surname, position: i === 0 ? 2 : 3, email: undefined as any, phone: undefined as any, eventId: p.eventId, eventTitle: p.eventTitle }))
        ];
        this.winners.set(podium);
        this.phase.set('podium');
        this.showPodium.set(true);
        this.revealStep.set(3);
        this.notificationService.notifySuccess('¡Ganador seleccionado!');
      }
    }, 120);
  }

  private pickTwoOthers(list: ParticipantDTO[], excludeIndex: number): ParticipantDTO[] {
    const pool = list.filter((_, i) => i !== excludeIndex);
    if (pool.length === 0) return [];
    const first = pool[Math.floor(Math.random() * pool.length)];
    const remaining = pool.filter(p => p !== first);
    const second = remaining.length ? remaining[Math.floor(Math.random() * remaining.length)] : undefined;
    return second ? [first, second] : [first];
  }
}