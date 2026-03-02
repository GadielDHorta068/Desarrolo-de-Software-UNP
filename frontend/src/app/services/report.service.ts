import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { StatusEvent } from '../models/events.model';

// {
//         "id": 1,
//         "mailUserReport": "alex6tc90@gmail.com",
//         "reason": "Porque se me canta",
//         "statusReport": "EARRING",
//         "timestamp": "2025-11-06 11:05:33"
//     }
export interface Report {
  id: Number,
  mailUserReport: string,
  reason: string,
  statusReport: StatusReport,
  createdAt: string
}

// {
//   "eventId": 25,
//   "mailUserReport": "alex6tc90@gmail.com",
//   "reason": "Porque se me canta"
// }
export interface InformReport {
  eventId: Number,
  mailUserReport: string,
  reason: string
}

export interface AdminEventReport {
  id: Number,
  eventId: Number,
  eventTitle: string,
  eventDate: string,
  statusEvent: StatusEvent,
  statusReport: StatusReport,
  totalReports: Number,
  score: Number,
  createdAt: string,
  adminNotes: string | null,
  reports: ReviewReportDTO[] | null,
}

export enum StatusReport {
  EARRING = 'EARRING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface MessageResponse {
  message: string;
}

export interface OrganizerEventReport {
  eventId: Number,
  eventTitle: string,
  statusEvent: StatusEvent
}

export interface ReviewReportDTO{
  id: Number,
  mailUserReport: string,
  reason: string,
  statusReport: StatusReport,
  createdAt: string
}


@Injectable({
  providedIn: 'root'
})
export class ReportService {
   private apiUrl = `${environment.apiUrl}/api/reports`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ){}

   // crea un reporte con los datos recibidos
  informReport(informReport: InformReport): Observable<InformReport> {
    const headers = this.getAuthHeaders();
    return this.http.post<InformReport>(`${this.apiUrl}`, informReport, { headers });
  }

  // Obtener los reportes agrupados por evento para el admin
  getAllReportedEvents() : Observable<AdminEventReport[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<AdminEventReport[]>(`${this.apiUrl}/event`, { headers });
  }

  // Obtener los detalles (reportes individuales) de un evento específico para el admin
  getEventReportDetails(eventId: string): Observable<AdminEventReport>{
    const headers = this.getAuthHeaders();
    return this.http.get<AdminEventReport>(`${this.apiUrl}/event/${eventId}`, { headers });
  }

  // Notificar al creador del evento sobre el reporte
  notifyCreatorAboutReports(eventId: string, adminMessage: string): Observable<MessageResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<MessageResponse>(
      `${this.apiUrl}/event/${eventId}/notify-creator?adminMessage=${encodeURIComponent(adminMessage)}`,
       {},
       { headers });
  }

  makeFinalDecisionOnEvent(eventId: string, finalStatusReport: StatusReport, adminMessage: string): Observable<OrganizerEventReport>{
    const headers = this.getAuthHeaders();
    return this.http.put<OrganizerEventReport>(
      `${this.apiUrl}/event/${eventId}/final-decision?finalStatusReport=${finalStatusReport}&adminMessage=${encodeURIComponent(adminMessage)}`, 
      {}, 
      { headers });
  }

  // resolve report
  reviewReport(reportId: string, status: StatusReport): Observable<ReviewReportDTO> {
    const headers = this.getAuthHeaders();
    return this.http.put<ReviewReportDTO>(
      `${this.apiUrl}/${reportId}/review?status=${status}`, 
      {}, 
      { headers });
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
