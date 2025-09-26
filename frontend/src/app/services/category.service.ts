import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface Category {
  id: number;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.apiUrl}/categories/`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // recupera todos los eventos creados (usado para test rapido)
  getAll(): Observable<Category[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<Category[]>(`${this.apiUrl}searchAll`, { headers });
  }
}