import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

// {
//         "id": 1,
//         "eventId": 25,
//         "eventName": "El evento",
//         "mailUserReport": "alex6tc90@gmail.com",
//         "reason": "Porque se me canta",
//         "statusReport": "EARRING",
//         "timestamp": "2025-11-06 11:05:33"
//     }
export interface Report {
  id: Number,
  eventId: Number,
  eventName: string,
  mailUserReport: string,
  reason: string,
  statusReport: string,
  timestamp: string
}

// {
//   "eventId": 25,
//   "mailUserReport": "alex6tc90@gmail.com",
//   "eventName": "El evento",
//   "reason": "Porque se me canta"
// }
export interface InformReport {
  eventId: Number,
  eventName: string,
  mailUserReport: string,
  reason: string
}


@Injectable({
  providedIn: 'root'
})
export class ReportService {
   private apiUrl = `${environment.apiUrl}/reports`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ){}

  getReportsByEvent(eventId: string): Observable<Report[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Report[]>(`${this.apiUrl}/review/event/${eventId}`, { headers });
  }

  // crea un reporte con los datos recibidos
  informReport(informReport: InformReport): Observable<Report[]> {
    const headers = this.getAuthHeaders();
    return this.http.post<Report[]>(`${this.apiUrl}/create`, informReport, { headers });
  }

  // resolve report
  resolveReport(reportId: string, eventId: string, status: string): Observable<Report> {
    const headers = this.getAuthHeaders();
    return this.http.put<Report>(`${this.apiUrl}/review?reportId=${reportId}&eventId=${eventId}&status=${status}`, { headers });
  }

  // determina si el usuario ya ha reportado el evento
  hasReportedEvent(eventId: string, mailUser: string): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.get<boolean>(`${this.apiUrl}/has-reported?eventId=${eventId}&userMail=${mailUser}`, { headers });
  }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
  } 

}
