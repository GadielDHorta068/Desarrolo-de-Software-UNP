import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/auditlog.model';

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

  getAuditLogs(nickname: string): Observable<AuditLog[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<AuditLog[]>(`${this.apiUrl}/obtain/nickname/${nickname}`, { headers });
  }

  // recupera los ganadores de un evento segun su id
  getAuditWinners(idEvent: string): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<WinnersAudit[]>(`${this.apiUrl}/obtain/event/${idEvent}/winners`, { headers });
  }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
  }
}
