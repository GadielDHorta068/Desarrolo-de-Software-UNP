import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Events, EventsCreate, EventsTemp, EventTypes, RaffleCreate, RaffleParticipantDTO, StatusEvent } from '../models/events.model';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { UserDTO } from '../models/UserDTO';
import { Response } from '../models/response.model';

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
  createEvent(creatorId: string, event: EventsCreate, eventType: string): Observable<EventsTemp> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    // return this.http.post<EventsTemp[]>(`${this.apiUrl}/create/giveaway/${creatorId}`, event, { headers });
    return this.http.post<EventsTemp>(`${this.apiUrl}/create/${eventType}/${creatorId}`, event, { headers });
  }

  // recupera los datos de un evento segun el id recibido
  getEventById(eventId: string): Observable<EventsTemp> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<EventsTemp>(`${this.apiUrl}/id/${eventId}`, { headers });
  }

  // recupera el estado de un evento segun el id recibido
  getStatusEventById(eventId: string): Observable<Response> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<Response>(`${this.apiUrl}/status/id/${eventId}`, { headers });
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

  // actualiza los datos del evento, sean sorteos o rifas
  updateEvents(dataEvent: EventsCreate, eventId: string, userId: number|undefined): Observable<any> {
    if(!userId){
      console.warn("Error al actualizar el evento. Se espera el id de un usuario.");
      return throwError(() => new Error("Se espera el id de un usuario."));
    }
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    let pathEventType = (dataEvent.eventType == EventTypes.GIVEAWAY) ? "giveaway" : "raffle";
    return this.http.put<any>(`${this.apiUrl}/update/${pathEventType}/${eventId}/user/${userId}`, dataEvent, { headers });
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

  // Obtiene la lista de ganadores del evento (independiente de auditoría y sin finalizar)
  getWinnersListByEventId(eventId: number): Observable<import('../models/winner.model').WinnerDTO[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<import('../models/winner.model').WinnerDTO[]>(`${this.apiUrl}/winners/event/${eventId}/list`, { headers });
  }

  // Obtiene todos los participantes del evento (para suspense)
  getParticipantsByEventId(eventId: number): Observable<any[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<any[]>(`${this.apiUrl}/participants/event/${eventId}`, { headers });
  }

    getSoldNumbersByRaffleId(aRaffleId: number): Observable<number[]> {
        return this.http.get<number[]>(`${this.apiUrl}/raffle/${aRaffleId}/sold-numbers`);
    }

    getParticipantUsersByEventId(anEventId: number, anEventType: EventTypes, aUserEmail: string): Observable<RaffleParticipantDTO[]> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.authService.getToken()}`,
            'Content-Type': 'application/json'
        });
        if (anEventType != EventTypes.RAFFLES) {
            return this.http.get<RaffleParticipantDTO[]>(`${this.apiUrl}/${anEventId}/get-users-participants`, { headers, 
                params: { aUserEmail }
            });
        }
        // return this.http.get<UserDTO[]>(`${this.apiUrl}/${anEventId}/get-raffle-owners`, { headers });
        return this.http.get<RaffleParticipantDTO[]>(`${this.apiUrl}/${anEventId}/get-raffle-participants`, { headers,
            params: { aUserEmail }
        });
    }
    // USAR RAFFLE PARTICIPANT PARA AMBOS CASOS.

  // Buscar eventos por título (endpoint público)
  searchEvents(title: string): Observable<EventsTemp[]> {
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/search`, { params: { title } });
  }

  // Obtener eventos por estado (endpoint público)
  getEventsByStatus(status: StatusEvent): Observable<EventsTemp[]> {
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/status/${status}`);
  }
 //Obtener eventos por tipo (endpoint público)
  getEventsByType(type: EventTypes): Observable<EventsTemp[]> {
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/type/${type}`);
  }

  // Obtener eventos activos con filtros progresivos (endpoint público)
  getActiveEvents(options: {
    type?: EventTypes;
    categorie?: string;
    start?: string; // formato ISO: YYYY-MM-DD
    end?: string;   // formato ISO: YYYY-MM-DD
    winnerCount?: number;
    emailUserRegister?: string;
  } = {}): Observable<EventsTemp[]> {
    const params: any = {};
    if (options.type) params['type'] = options.type;
    if (options.categorie) params['categorie'] = options.categorie;
    if (options.start) params['start'] = options.start;
    if (options.end) params['end'] = options.end;
    if (options.winnerCount !== undefined && options.winnerCount !== null) params['winnerCount'] = options.winnerCount;
    if( options.emailUserRegister) params['emailUserRegister'] = options.emailUserRegister;
    console.log('Fetching active events with params:', params);
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/active`, { params });
  }


}