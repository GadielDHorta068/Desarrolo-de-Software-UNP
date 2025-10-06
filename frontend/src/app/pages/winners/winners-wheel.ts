import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { NotificationService } from '../../services/notification.service';
import { LottieComponent, AnimationOptions } from 'ngx-lottie';
import confetti from 'canvas-confetti';

@Component({
  selector: 'app-winners-wheel',
  standalone: true,
  imports: [CommonModule, LottieComponent],
  templateUrl: './winners-wheel.html',
  styleUrl: './winners-wheel.css'
})
export class WinnersWheel {
  eventId!: number;
  spinning = true;
  loading = true;
  winners: any[] = [];
  errorMessage: string | null = null;
  highlightIndex = -1;
  options: AnimationOptions = {
    // Puedes cambiar a un asset local si agregas 'src/assets/roulette.json'
    path: 'https://assets9.lottiefiles.com/packages/lf20_khzniaya.json',
    loop: true,
    autoplay: true
  };

  constructor(
    private route: ActivatedRoute,
    private eventsService: EventsService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.eventId = Number(this.route.snapshot.paramMap.get('eventId'));
    if (!this.eventId || isNaN(this.eventId)) {
      this.errorMessage = 'Evento inválido';
      this.spinning = false;
      this.loading = false;
      return;
    }

    // Iniciar animación de ruleta y solicitar ganadores
    this.fetchWinners();
  }

  private fetchWinners() {
    this.loading = true;
    this.spinning = true;
    this.eventsService.getWinnersByEventId(this.eventId).subscribe({
      next: (response: any[]) => {
        this.winners = response || [];
        // Simular tiempo de giro antes de mostrar ganadores
        setTimeout(() => {
          this.spinning = false;
          this.loading = false;
          this.playWinnerReveal();
          this.notificationService.notifySuccess('Ganadores obtenidos');
        }, 3500);
      },
      error: (err) => {
        this.spinning = false;
        this.loading = false;
        const msg = err?.error?.message || err?.message || 'Error al finalizar el evento';
        this.errorMessage = msg;
        this.notificationService.notifyError(msg);
      }
    });
  }

  // Animación de selección: resalta progresivamente y termina en el primer ganador
  private playWinnerReveal() {
    if (!this.winners.length) return;
    let i = 0;
    const steps = Math.min(this.winners.length, 10);
    const interval = setInterval(() => {
      this.highlightIndex = i % this.winners.length;
      i++;
      if (i >= steps) {
        clearInterval(interval);
        // Destacar al ganador principal (posición 1)
        const mainIndex = 0;
        this.highlightIndex = mainIndex;
        confetti({ particleCount: 120, spread: 70, origin: { y: 0.6 } });
      }
    }, 250);
  }
}