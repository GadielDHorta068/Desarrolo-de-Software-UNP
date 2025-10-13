import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { GuestUser } from '../pages/questionary/guestUser';
import { EventTypes } from '../models/events.model';
import { BuyRaffleNumberDTO } from '../models/buyRaffleNumberDTO';

@Injectable({
  providedIn: 'root'
})

export class QuestionaryService {

    private API_URL = `${environment.apiUrl}/events`;

    constructor (
        private http: HttpClient
    ) {}

    save(aGuestUser: GuestUser, aEventId: number): Observable<any> {
        
        return this.http.post<any>(
            `${this.API_URL}/${aEventId}/participants`,
            aGuestUser
        );
        
        
    }

    saveRaffleNumber(aEventId: number, buyRaffleNumberRequest: BuyRaffleNumberDTO): Observable<any> {
        return this.http.post<any>(
            `${this.API_URL}/${aEventId}/buy-raffle-number`,
            buyRaffleNumberRequest
        );
    }

}
