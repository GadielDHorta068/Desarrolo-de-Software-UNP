import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Events, EventsCreate, EventsTemp, RaffleCreate, StatusEvent } from '../models/events.model';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class EventsService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // crea eventos del tipo SORTEO
  // createEvent(creatorId: string, event: EventsCreate|RaffleCreate, eventType: string): Observable<EventsTemp[]> {
  createEvent(creatorId: string, event: EventsCreate, eventType: string): Observable<EventsTemp[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    // return this.http.post<EventsTemp[]>(`${this.apiUrl}/create/giveaway/${creatorId}`, event, { headers });
    return this.http.post<EventsTemp[]>(`${this.apiUrl}/create/${eventType}/${creatorId}`, event, { headers });
  }

  // recupera los datos de un evento segun el id recibido
  getEventById(eventId: string): Observable<EventsTemp> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<EventsTemp>(`${this.apiUrl}/id/${eventId}`, { headers });
  }

  getEventsByParticipantId(userId: number): Observable<Events[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<Events[]>(`${this.apiUrl}/participant/${userId}`, { headers });
  }

  // recupera todos los eventos creados (usado para test rapido)
  getAllByCreator(id: string): Observable<EventsTemp[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/creator/`+id, { headers });
  }

  // recupera todos los eventos creados (usado para test rapido)
  getAllEvents(): Observable<EventsTemp[]> {
    // Endpoint público: no requiere encabezados de autorización
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/all`);
  }

  // recupera todos los tipos de eventos permitidos
  getTypesEvent(): Observable<string[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<string[]>(`${this.apiUrl}/event-types`, { headers });
  }

  // actualiza los datos del evento
  updateGiveaways(dataEvent: EventsCreate, eventId: string, userId: number|undefined): Observable<EventsTemp[]> {
    if(!userId){
      console.warn("Error al actualizar el evento. Se espera el id de un usuario.");
      return throwError(() => new Error("Se espera el id de un usuario."));
    }
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<EventsTemp[]>(`${this.apiUrl}/update/giveaway/${eventId}/user/${userId}`, dataEvent, { headers });
  }

// Actualiza el estado de un evento (ABIERTO/CERRADO/FINALIZADO/BLOQUEADO)
  updateEventStatus(eventId: number, userId: number, status: StatusEvent): Observable<EventsTemp> {
    const payload = { statusEvent: status };
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<EventsTemp>(`${this.apiUrl}/${eventId}/status/user/${userId}`, payload, { headers });
  }

  // Obtiene ganadores del evento y finaliza en backend si corresponde
  getWinnersByEventId(eventId: number): Observable<any[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<any[]>(`${this.apiUrl}/winners/event/${eventId}`, { headers });
  }

  // Obtiene todos los participantes del evento (para suspense)
  getParticipantsByEventId(eventId: number): Observable<any[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<any[]>(`${this.apiUrl}/participants/event/${eventId}`, { headers });
  }
}