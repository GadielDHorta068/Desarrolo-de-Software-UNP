import { Injectable } from '@angular/core';
import { reviewFromFrontToBackDTO } from '../models/review/reviewFromFrontToBackDTO';
import { firstValueFrom, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';
import { reviewFromBacktoFrontDTO } from '../models/review/reviewFromBacktoFrontDTO';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

    private apiUrl = `${environment.apiUrl}/reviews`;
    
    // private avgUsersScore: Record<string, number> = {
    //     "mail1@gmail.com": 4,
    //     "mail2@gmail.com": 3,
    //     "mail3@gmail.com": 5
    // };
    private avgUsersScore: Record<string, number> = {};

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

    getAvgScoreByUserEmail(aUserEmail: string): Observable<any> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.authService.getToken()}`,
            'Content-Type': 'application/json'
        });
        return this.http.get<number>(`${this.apiUrl}/avg-score`, {
            headers,
            params: { email: aUserEmail }
        })
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

    // segunda version de recuperacion de puntuacion de un usuario, usamos una especie de cache para evitar multiples llamadas
    async getOrFetchAvgScore(email: string): Promise<number> {
        // revisamos si existe en el record
        if (email in this.avgUsersScore) {
            return this.avgUsersScore[email];
        }

        // si no existe vamos al server
        const scoreFromServer = await firstValueFrom(this.getAvgScoreByUserEmail(email));
        this.avgUsersScore[email] = scoreFromServer;
        return scoreFromServer;
    }
}
