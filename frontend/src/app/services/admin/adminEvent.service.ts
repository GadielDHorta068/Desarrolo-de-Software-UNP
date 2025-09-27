import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Events, EventsTemp } from '../../models/events.model';

@Injectable({
  providedIn: 'root'
})
export class AdminEventService {

  // evento en contexto (seleccionado)
  // private selectedEventSubject = new BehaviorSubject<Events | null>(null);
  private selectedEventSubject = new BehaviorSubject<EventsTemp | null>(null);
  public selectedEvent$ = this.selectedEventSubject.asObservable();

  constructor() {}

  // public setSelectedEvent(event: Events|null){
  public setSelectedEvent(event: EventsTemp|null){
    this.selectedEventSubject.next(event);
  }

  
}