import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { GuestUser } from '../pages/questionary/guestUser';
import { EventTypes } from '../models/events.model';
import { BuyRaffleNumberDTO } from '../models/buyRaffleNumberDTO';
import { UserDTO } from '../models/UserDTO';

@Injectable({
  providedIn: 'root'
})

export class QuestionaryService {

    private API_URL = `${environment.apiUrl}/events`;

    constructor (
        private http: HttpClient
    ) {}

    save(aGuestUser: UserDTO, aEventId: number): Observable<any> {
        const invite = (typeof window !== 'undefined')
            ? new URLSearchParams(window.location.search).get('invite')
            : null;
        const options = (invite && invite.trim().length > 0)
            ? { params: new HttpParams().set('invite', invite) }
            : {};
        return this.http.post<any>(
            `${this.API_URL}/${aEventId}/participants`,
            aGuestUser,
            options
        );
    }

    saveRaffleNumber(aEventId: number, buyRaffleNumberRequest: BuyRaffleNumberDTO): Observable<any> {
        const invite = (typeof window !== 'undefined')
            ? new URLSearchParams(window.location.search).get('invite')
            : null;
        const options = (invite && invite.trim().length > 0)
            ? { params: new HttpParams().set('invite', invite) }
            : {};
        return this.http.post<any>(
            `${this.API_URL}/${aEventId}/buy-raffle-number`,
            buyRaffleNumberRequest,
            options
        );
    }

}
