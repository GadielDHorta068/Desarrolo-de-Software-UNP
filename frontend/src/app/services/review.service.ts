import { Injectable } from '@angular/core';
import { reviewFromFrontToBackDTO } from '../models/review/reviewFromFrontToBackDTO';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';
import { reviewFromBacktoFrontDTO } from '../models/review/reviewFromBacktoFrontDTO';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

    private apiUrl = `${environment.apiUrl}/reviews`;

    constructor(
        private http: HttpClient,
        private authService: AuthService
  ) {}

    getReviewsByUserEmail(aUserEmail: string): Observable<any> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.authService.getToken()}`,
            'Content-Type': 'application/json'
        });
        return this.http.get<reviewFromBacktoFrontDTO[]>(`${this.apiUrl}/user/${aUserEmail}`, { headers });
    }

    createReview(aReview: reviewFromFrontToBackDTO, eventId: string): Observable<any> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.authService.getToken()}`,
            'Content-Type': 'application/json'
        });
        console.log('Token enviado:', this.authService.getToken());
        return this.http.post<reviewFromFrontToBackDTO>(`${this.apiUrl}/event/${eventId}/create-review`,
            aReview,
            { headers }
        );
    }
}
