import { CommonModule } from '@angular/common';
import { Component, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { NotificationService } from '../../services/notification.service';
import confetti from 'canvas-confetti';
import { WinnerDTO } from '../../models/winner.model';
import { ParticipantDTO } from '../../models/participant.model';

@Component({
  selector: 'app-winners-wheel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './winners-wheel.html',
  styleUrl: './winners-wheel.css'
})
export class WinnersWheel {
  eventId!: number;
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  participants = signal<ParticipantDTO[]>([]);
  winners = signal<WinnerDTO[]>([]);

  // Fases: loading → shuffle → reveal → podium → final
  phase = signal<'loading' | 'shuffle' | 'reveal' | 'podium' | 'final'>('loading');
  spotlightIndex = signal(-1);
  podiumVisible = signal(false);

  // Derivados
  tickerNames = computed(() => {
    const base = this.participants().map(p => `${p.name} ${p.surname}`);
    if (base.length === 0) return this.winners().map(w => `${w.name} ${w.surname}`);
    return base;
  });

  tickerLoop = computed(() => {
    const names = this.tickerNames();
    return [...names, ...names, ...names.slice(0, Math.min(10, names.length))];
  });

  mainWinner = computed(() => {
    const ws = this.winners();
    return ws.find(w => w.position === 1) || ws[0];
  });

  podium = computed(() => {
    return [...this.winners()].filter(w => w.position <= 3).sort((a,b) => a.position - b.position);
  });

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

    this.eventsService.getParticipantsByEventId(this.eventId).subscribe({
      next: (ps: ParticipantDTO[]) => {
        this.participants.set(ps || []);
        this.eventsService.getWinnersByEventId(this.eventId).subscribe({
          next: (ws: WinnerDTO[]) => {
            const sorted = (ws || []).sort((a,b) => a.position - b.position);
            this.winners.set(sorted);
            this.loading.set(false);
            this.startSequence();
          },
          error: (err) => this.handleError(err, 'Error al obtener ganadores')
        });
      },
      error: (err) => this.handleError(err, 'Error al obtener participantes')
    });
  }

  private handleError(err: any, fallback: string) {
    this.loading.set(false);
    const msg = err?.error || err?.error?.message || err?.message || fallback;
    this.errorMessage.set(msg);
    this.notificationService.notifyError(msg);
  }

  private startSequence() {
    // Etapa 1: ticker/shuffle breve
    this.phase.set('shuffle');
    setTimeout(() => this.runSpotlightReveal(), 1200);
  }

  private runSpotlightReveal() {
    const winners = this.winners();
    if (!winners.length) {
      this.phase.set('final');
      return;
    }

    this.phase.set('reveal');
    const revealEachMs = 1200;

    winners.forEach((w, i) => {
      setTimeout(() => {
        this.spotlightIndex.set(i);
        confetti({ particleCount: 80, spread: 60, origin: { y: 0.4 } });
        if (i === winners.length - 1) {
          setTimeout(() => {
            this.podiumVisible.set(true);
            this.phase.set('podium');
            setTimeout(() => this.phase.set('final'), 1200);
            this.notificationService.notifySuccess('¡Ganadores revelados!');
          }, 700);
        }
      }, revealEachMs * i);
    });
  }
}