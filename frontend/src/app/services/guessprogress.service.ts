import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParticipantRequestDTO } from '../models/participantRequestDTO';
import { CheckGuessNumberDTO } from '../models/checkGuessNumberDTO';
import { GuessCheckResponseDTO } from '../models/guessCheckResponseDTO';

@Injectable({
  providedIn: 'root',
})
export class GuessprogressService {

  private apiUrl= `${environment.apiUrl}/api/contest`;

  constructor(
    private http: HttpClient,
  ){}

  // registra un usuario una vez terminado de adivinar el número
  // el backend se encargará de ver si es un usuario registrado o invitado
  registerEvent(contestId: number, dto: ParticipantRequestDTO): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${contestId}/participants`, dto);
  }

  // Chequea si el usuario ya participó en el evento
  checkParticipation(contestId: number, email: string): Observable<GuessCheckResponseDTO>{
    return this.http.get<GuessCheckResponseDTO>(`${this.apiUrl}/check-participant/guess/${contestId}`, {
      params : {email}
    });
  }

  // Chequea el número ingresado (mayor, menor, igual al número objetivo)
  checkGuessNumber(contestId: number, guessedNumber: number): Observable<CheckGuessNumberDTO> {
    return this.http.get<CheckGuessNumberDTO>(
      `${this.apiUrl}/${contestId}/guess/check-number`,
      { params: { guessedNumber } }
    );
  }

}
