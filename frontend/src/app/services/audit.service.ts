import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { AuditAction, AuditActionType, AuditEvent } from '../models/auditevent.model';
import { EventTypes } from '../models/events.model';

export interface WinnersAudit {
  id: Number,
  userEmail: string
  userLastName: string,
  userName: string,
  userPhone: string
  userPosition: Number
}


@Injectable({
  providedIn: 'root'
})
export class AuditService {
   private apiUrl = `${environment.apiUrl}/audit`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ){}

  getAuditLogs(nickname: string): Observable<AuditEvent[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<AuditEvent[]>(`${this.apiUrl}/obtain/nickname/${nickname}`, { headers });
  }

  // recupera los ganadores de un evento segun su id
  getAuditWinners(idEvent: string): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<WinnersAudit[]>(`${this.apiUrl}/event/winners/${idEvent}`, { headers });
  }

  /**
   * Filtra los eventos de auditoria segun los parametros recibidos
   * @param creator
   * @param type
   * @param from
   * @param to
   * @returns
   */
  getFilterAuditEvent(creator: string, type?: EventTypes | null, from?: Date, to?: Date): Observable<AuditEvent[]>{
    const headers = this.getAuthHeaders();
    let params = new HttpParams();

    if (type != null) params = params.set('eventType', String(type));
    if (from) params = params.set('from', from.toISOString().split('T')[0]);
    if (to) params = params.set('to', to.toISOString().split('T')[0]);

    const url = `${this.apiUrl}/filter/event/${encodeURIComponent(creator)}`;
    return this.http.get<AuditEvent[]>(url, { headers, params });
  }

  /**
   * Filtrar las acciones de auditoria segun los parametros recibidos
   * @param eventId 
   * @param action 
   * @param from 
   * @param to 
   * @returns 
   */
  getActionByFilters(eventId: number, action?: AuditActionType | null, from?: Date, to?: Date): Observable<AuditAction[]>{
    const headers = this.getAuthHeaders();
    let params = new HttpParams();
    
    if (action != null) params = params.set('actionType', String(action));
    if (from) params = params.set('from', from.toISOString());
    if (to) params = params.set('to', to.toISOString());
    const url = `${this.apiUrl}/filter/action/${eventId}`;
    return this.http.get<AuditAction[]>(url, { headers, params });
  }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
  } 
}
