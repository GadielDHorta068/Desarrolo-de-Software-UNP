import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventsService } from '../../services/events.service';
import { EventsTemp } from '../../models/events.model';
import { DrawCard } from '../../shared/components/draw-card/draw-card';

@Component({
  selector: 'app-public-events',
  standalone: true,
  imports: [CommonModule, DrawCard],
  templateUrl: './public-events.html',
  styleUrl: './public-events.css'
})
export class PublicEvents implements OnInit {
  events: EventsTemp[] = [];
  loading = true;
  error = '';

  constructor(
    private eventsService: EventsService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    this.loadPublicEvents();
  }

  private loadPublicEvents(): void {
    this.eventsService.getAllEvents().subscribe({
      next: (response) => {
        this.events = response || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('[PublicEvents] Error al recuperar eventos públicos:', err);
        this.error = 'No se pudieron cargar los eventos.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
