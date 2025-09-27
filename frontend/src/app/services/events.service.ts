import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Events, EventsCreate, EventsTemp } from '../models/events.model';
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

  createEvent(creatorId: string, event: EventsCreate): Observable<EventsTemp[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<EventsTemp[]>(`${this.apiUrl}/create/giveaway/${creatorId}`, event, { headers });
  }

  getEventsByParticipantId(userId: number): Observable<Events[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<Events[]>(`${this.apiUrl}/participant/${userId}`, { headers });
  }

  // recupera todos los eventos creados (usado para test rapido)
  getAllEvents(): Observable<EventsTemp[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<EventsTemp[]>(`${this.apiUrl}/all`, { headers });
  }
}