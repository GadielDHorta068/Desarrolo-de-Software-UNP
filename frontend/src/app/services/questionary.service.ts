import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { GuestUser } from '../pages/questionary/guestUser';
import { EventTypes } from '../models/events.model';
import { BuyRaffleNumberDTO } from '../models/buyRaffleNumberDTO';
import { UserDTO } from '../models/UserDTO';
import { ParticipantRequestDTO } from '../models/participantRequestDTO';

@Injectable({
  providedIn: 'root'
})

export class QuestionaryService {

    private API_URL = `${environment.apiUrl}/events`;
    private apiUrl = `${environment.apiUrl}/api/contest`;

    constructor (
        private http: HttpClient
    ) {}

    save(aGuestUser: UserDTO, aEventId: number): Observable<any> {
        const invite = this.getInviteToken();
        const url = (invite && invite.trim().length > 0)
            ? `${this.API_URL}/${aEventId}/participants?invite=${encodeURIComponent(invite)}`
            : `${this.API_URL}/${aEventId}/participants`;
        return this.http.post<any>(url, aGuestUser);
    }

    saveGuessProgress(contestId: number, participantRequestDTO: ParticipantRequestDTO): Observable<any> {
        const invite = this.getInviteToken();
        const url = (invite && invite.trim().length > 0) 
        ? `${this.apiUrl}/${contestId}/participants?invite=${encodeURIComponent(invite)}`
        : `${this.apiUrl}/${contestId}/participants`;
        return this.http.post<any>(url,participantRequestDTO);
    }

    saveRaffleNumber(aEventId: number, buyRaffleNumberRequest: BuyRaffleNumberDTO): Observable<any> {
        const invite = this.getInviteToken();
        const url = (invite && invite.trim().length > 0)
            ? `${this.API_URL}/${aEventId}/buy-raffle-number?invite=${encodeURIComponent(invite)}`
            : `${this.API_URL}/${aEventId}/buy-raffle-number`;
        return this.http.post<any>(url, buyRaffleNumberRequest);
    }

    private getInviteToken(): string | null {
        const fromUrl = (typeof window !== 'undefined')
            ? new URLSearchParams(window.location.search).get('invite')
            : null;
        if (fromUrl && fromUrl.trim().length > 0) return fromUrl;
        try {
            const stored = localStorage.getItem('invite_token');
            return stored && stored.trim().length > 0 ? stored : null;
        } catch {
            return null;
        }
    }

}
