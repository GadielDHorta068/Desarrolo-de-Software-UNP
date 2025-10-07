import { CommonModule } from '@angular/common';
import { Component, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { NotificationService } from '../../services/notification.service';
import { LottieComponent, AnimationOptions } from 'ngx-lottie';
import confetti from 'canvas-confetti';
import { WinnerDTO } from '../../models/winner.model';
import { ParticipantDTO } from '../../models/participant.model';

@Component({
  selector: 'app-winners-wheel',
  standalone: true,
  imports: [CommonModule, LottieComponent],
  templateUrl: './winners-wheel.html',
  styleUrl: './winners-wheel.css'
})
export class WinnersWheel {
  eventId!: number;
  
  // Signals para manejo de estado reactivo
  spinning = signal(true);
  loading = signal(true);
  winners = signal<WinnerDTO[]>([]);
  participants = signal<ParticipantDTO[]>([]);
  errorMessage = signal<string | null>(null);
  highlightIndex = signal(-1);
  revealStep = signal(0);
  showPodium = signal(false);
  
  // Computed para obtener el ganador principal
  mainWinner = computed(() => {
    const winners = this.winners();
    return winners.find(w => w.position === 1) || winners[0];
  });
  
  // Computed para obtener el podio (primeros 3)
  podium = computed(() => {
    const winners = this.winners();
    return winners
      .filter(w => w.position <= 3)
      .sort((a, b) => a.position - b.position);
  });
  
  options: AnimationOptions = {
    // Animación de ruleta más dramática
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
      this.errorMessage.set('Evento inválido');
      this.spinning.set(false);
      this.loading.set(false);
      return;
    }

    // Iniciar animación de ruleta y solicitar ganadores
    this.fetchWinners();
  }

  private fetchWinners() {
    this.loading.set(true);
    this.spinning.set(true);
    
    // Primero obtener todos los participantes para el suspense
    this.eventsService.getParticipantsByEventId(this.eventId).subscribe({
      next: (participantsResponse: ParticipantDTO[]) => {
        this.participants.set(participantsResponse || []);
        
        // Luego obtener los ganadores
        this.eventsService.getWinnersByEventId(this.eventId).subscribe({
          next: (winnersResponse: WinnerDTO[]) => {
            this.winners.set(winnersResponse || []);
            // Simular tiempo de giro antes de mostrar ganadores
            setTimeout(() => {
              this.spinning.set(false);
              this.loading.set(false);
              this.playWinnerReveal();
              this.notificationService.notifySuccess('¡Ganadores revelados!');
            }, 4000); // Aumentado para más dramatismo
          },
          error: (err) => {
            this.spinning.set(false);
            this.loading.set(false);
            const msg = err?.error?.message || err?.message || 'Error al obtener ganadores';
            this.errorMessage.set(msg);
            this.notificationService.notifyError(msg);
          }
        });
      },
      error: (err) => {
        this.spinning.set(false);
        this.loading.set(false);
        const msg = err?.error?.message || err?.message || 'Error al obtener participantes';
        this.errorMessage.set(msg);
        this.notificationService.notifyError(msg);
      }
    });
  }

  // Animación dramática de revelación de ganadores
  private playWinnerReveal() {
    const winners = this.winners();
    if (!winners.length) return;

    // Paso 1: Ruleta acelerando y desacelerando
    this.revealStep.set(1);
    
    // Paso 2: Mostrar participantes en orden aleatorio (efecto suspense)
    setTimeout(() => {
      this.revealStep.set(2);
      this.showRandomParticipants();
    }, 1000);

    // Paso 3: Revelar podio con efectos especiales
    setTimeout(() => {
      this.revealStep.set(3);
      this.showPodium.set(true);
      this.playPodiumReveal();
    }, 3000);
  }

  // Mostrar participantes aleatoriamente para crear suspense
  private showRandomParticipants() {
    const participants = this.participants();
    let count = 0;
    const maxShows = Math.min(participants.length * 2, 20); // Más iteraciones para más suspense
    
    const interval = setInterval(() => {
      const randomIndex = Math.floor(Math.random() * participants.length);
      this.highlightIndex.set(randomIndex);
      count++;
      
      if (count >= maxShows) {
        clearInterval(interval);
        this.highlightIndex.set(-1);
      }
    }, 150); // Más rápido para más dinamismo
  }

  // Revelación dramática del podio
  private playPodiumReveal() {
    const podium = this.podium();
    
    // Efectos de confetti en cascada
    setTimeout(() => {
      // Confetti para el ganador (posición 1)
      if (podium[0]) {
        confetti({
          particleCount: 150,
          spread: 60,
          origin: { y: 0.3 },
          colors: ['#FFD700', '#FFA500', '#FF6347', '#32CD32', '#1E90FF']
        });
      }
    }, 500);

    // Confetti adicional para el segundo lugar
    setTimeout(() => {
      if (podium[1]) {
        confetti({
          particleCount: 100,
          spread: 45,
          origin: { y: 0.4 },
          colors: ['#C0C0C0', '#FFD700', '#FFA500']
        });
      }
    }, 1500);

    // Confetti para el tercer lugar
    setTimeout(() => {
      if (podium[2]) {
        confetti({
          particleCount: 80,
          spread: 40,
          origin: { y: 0.5 },
          colors: ['#CD7F32', '#C0C0C0', '#FFD700']
        });
      }
    }, 2500);

    // Efecto final de celebración
    setTimeout(() => {
      this.playCelebrationEffect();
    }, 3500);
  }

  // Efecto final de celebración
  private playCelebrationEffect() {
    // Confetti masivo final
    for (let i = 0; i < 5; i++) {
      setTimeout(() => {
        confetti({
          particleCount: 200,
          spread: 100,
          origin: { y: 0.6 },
          colors: ['#FFD700', '#FFA500', '#FF6347', '#32CD32', '#1E90FF', '#FF69B4', '#9370DB']
        });
      }, i * 200);
    }
  }
}