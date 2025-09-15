import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { GuestUser } from '../pages/questionary/guestUser';

@Injectable({
  providedIn: 'root'
})

export class QuestionaryService {

    private questionaryUrl = 'rest/user'

    constructor (
        private http: HttpClient
    ) {}

    save(aGuestUser: GuestUser, aEventId: number): Observable<any> {
        return this.http.post<any>(this.questionaryUrl, { aGuestUser, aEventId });
    }

}
