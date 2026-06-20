import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Region } from '../models/region';

@Injectable({
  providedIn: 'root'
})
export class RegionService {

    private API_URL = `${environment.apiUrl}/region`

    constructor (
        private http: HttpClient
    ) {}

    getAllRegions(): Observable<any> {
        return this.http.get<any>(`${this.API_URL}/all`);
    }

    getNonCountrieRegions(): Observable<any> {
        return this.http.get<Region[]>(`${this.API_URL}/non-countries`);
    }

    isUserRegionInsideEventRegion(eventRegionId: number, userRegionId: number): Observable<any> {
        return this.http.get<any>(`${this.API_URL}/is-region-contained/event/${eventRegionId}/user/${userRegionId}`);
    }
}
