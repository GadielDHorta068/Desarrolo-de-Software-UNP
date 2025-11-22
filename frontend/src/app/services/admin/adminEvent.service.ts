import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { WinnerDTO } from '../../models/winner.model';
import { EventsService } from '../events.service';
import { DataStatusEvent } from '../../models/response.model';

@Injectable({
  providedIn: 'root'
})
export class AdminEventService {

  // evento en contexto (seleccionado)
  // private selectedEventSubject = new BehaviorSubject<Events | null>(null);
  private selectedEventSubject = new BehaviorSubject<EventsTemp | null>(null);
  public selectedEvent$ = this.selectedEventSubject.asObservable();

  // maneja una referencia a los ganadores del evento seleccionado (WinnerDTO)
  private winnersEventSubject = new BehaviorSubject<WinnerDTO[]>([]);
  public winnersEvent$ = this.winnersEventSubject.asObservable();

  constructor(
    private eventService: EventsService
  ) {
    const savedEvent = localStorage.getItem('selectedEvent');
    if (savedEvent) {
      try {
        const parsedEvent = JSON.parse(savedEvent);
        this.selectedEventSubject.next(parsedEvent);
        // Optionally reload winners if needed, but might be better to do lazily or let component trigger
        if (parsedEvent?.statusEvent == StatusEvent.FINISHED) {
          this.loadWinners("" + parsedEvent.id);
        }
      } catch (e) {
        console.error('Error parsing selectedEvent from localStorage', e);
        localStorage.removeItem('selectedEvent');
      }
    }
  }

  public getSelectedEvent(): EventsTemp | null {
    return this.selectedEventSubject.getValue();
  }

  public setSelectedEvent(event: EventsTemp | null) {
    this.selectedEventSubject.next(event);
    if (event) {
      localStorage.setItem('selectedEvent', JSON.stringify(event));
    } else {
      localStorage.removeItem('selectedEvent');
    }

    if (event?.statusEvent == StatusEvent.FINISHED) {
      // console.log("[loadWinners] => Evento finalizado, vamos a buscar los ganadores!");
      // vamos a buscar los ganadores del evento
      this.loadWinners("" + event.id);
    }
  }

  // vamos a buscar los ganadores del sorteo si está FINALIZADO (independiente de auditoría)
  private loadWinners(eventId: string): void {
    this.eventService.getWinnersListByEventId(Number(eventId)).subscribe({
      next: (data) => {
        // console.log(`[loadWinners] => ganadores recuperados del evento ${eventId}: `, data);
        this.winnersEventSubject.next(data);
      },
      error: (err) => {
        console.error(`[loadWinners] => Error al obtener los ganadores del evento ${eventId}:`, err);
      }
    });
  }

}