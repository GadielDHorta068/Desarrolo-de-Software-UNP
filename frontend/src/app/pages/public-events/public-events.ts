import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EventsService } from '../../services/events.service';
import { EventsTemp, StatusEvent } from '../../models/events.model';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-public-events',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DrawCard],
  templateUrl: './public-events.html',
  styleUrl: './public-events.css'
})
export class PublicEvents implements OnInit {
  events: EventsTemp[] = [];
  filteredEvents: EventsTemp[] = [];
  loading = true;
  error = '';
  public StatusEvent = StatusEvent;
  selectedStatus: 'ALL' | StatusEvent = 'ALL';
  userLogged: boolean = false;

  constructor(
    private eventsService: EventsService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ){}

  ngOnInit(): void {
    this.loadPublicEvents();
    this.userLogged = this.authService.isAuthenticated();
  }

  private loadPublicEvents(): void {
    this.eventsService.getAllEvents().subscribe({
      next: (response) => {
        this.events = response || [];
        this.applyFilter(this.selectedStatus);
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

  public applyFilter(status: 'ALL' | StatusEvent): void {
    this.selectedStatus = status;
    if (status === 'ALL') {
      this.filteredEvents = this.events;
    } else {
      this.filteredEvents = this.events.filter(evt => evt.statusEvent === status);
    }
  }
}
