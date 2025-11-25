import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { UserDTO } from '../../models/UserDTO';
import { BuyRaffleNumberDTO } from '../../models/buyRaffleNumberDTO';
import { QuestionaryService } from '../questionary.service';
import { AdminEventService } from './adminEvent.service';
import { EventsService } from '../events.service';
import { DataStatusEvent } from '../../models/response.model';
import { AdminPaymentService, DataPayment } from './adminPayment.service';
import { AuthService } from '../auth.service';
import { GuessprogressService } from '../guessprogress.service';


@Injectable({
  providedIn: 'root'
})
export class AdminInscriptService {

  // flag de visualizacoin de modal de inscirpcion
  private openInscriptionSubject = new BehaviorSubject<boolean>(false);
  public openInscription$ = this.openInscriptionSubject.asObservable();

  // flag de visualizacoin de modal de rifas
  private openRaffleNUmbersSubject = new BehaviorSubject<boolean>(false);
  public openRaffleNUmbers$ = this.openRaffleNUmbersSubject.asObservable();

  // flag de visualizacoin de modal de adivinanzas
  private openGuessingSubject = new BehaviorSubject<boolean>(false);
  public openGuessing$ = this.openGuessingSubject.asObservable();

  // datos del usuario jugador
  private dataCurrentPlayerSubject = new BehaviorSubject<UserDTO | null>(null);
  public dataCurrentPlayer$ = this.dataCurrentPlayerSubject.asObservable();

  // evento en contexto
  event!: EventsTemp | null;

  selectedRaffleNumbers: number[] = [];

  constructor(
    private questionaryService: QuestionaryService,
    private adminEventService: AdminEventService,
    private eventService: EventsService,
    private adminPaymentService: AdminPaymentService,
    private authService: AuthService,
    private guessProgressService: GuessprogressService
  ) {
    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.event = currentEvent;
      }
    )
  }

  // administran la apertura de los modales de inscripcion y seleccion de nros
  setOpenModalInscript(open: boolean) {
    this.openInscriptionSubject.next(open);
  }
  setOpenModalRaffle(open: boolean) {
    this.openRaffleNUmbersSubject.next(open);
  }
  setOpenModalGuessing(open: boolean) {
    this.openGuessingSubject.next(open);
  }

  // inscribe al usuario al evento seleccionado
  async onInscript(user: UserDTO): Promise<void> {
    if (!this.event) return;

    try {
      if (this.event.eventType === EventTypes.RAFFLES) {
        // aca deberia primero simular el pago y de ahi realizar la compra
        const buyNumRequest: BuyRaffleNumberDTO = {
          aGuestUser: user,
          someNumbersToBuy: this.selectedRaffleNumbers
        };
        const response = await firstValueFrom(
          this.questionaryService.saveRaffleNumber(this.event.id, buyNumRequest)
        )
        // si es exitosa la inscripcion, guardamos los datos para la compra
        if (response.status == 200) {
          this.adminPaymentService.setDataPayment(this.getDataPayment(user, this.selectedRaffleNumbers));
        }
        let customResponse = { ...response };
        customResponse.redirectPay = true;
        return customResponse;
      }
      else {
        // sea sorteo o adivinanza, no requiere pago
        if (this.event.eventType == EventTypes.GUESSING_CONTEST) {
          const responseGuessing = await firstValueFrom(
            this.guessProgressService.checkParticipation(this.event.id, user.email)
          )
          // personaizamos la respuesta para poder abrir el modal de adlet customResponse = { ...response };
          let respInscript: any = { ...responseGuessing };
          // sea cual sea la respuesta si tiene
          if (respInscript.alreadyParticipated || respInscript.message) {
            this.dataCurrentPlayerSubject.next(user);
            respInscript.status = 200;
            respInscript.isGuessing = true;
            respInscript.allowed = !respInscript.alreadyParticipated;
          }
          return respInscript;
        }
        // por deafult sale por SORTEO
        const response = await firstValueFrom(this.questionaryService.save(user, this.event.id));
        return response;
      }
    } catch (errorResponse: any) {
      console.log('[adminInscript] => Error al comprar los números:', JSON.stringify(errorResponse));
      return Promise.resolve(errorResponse);
    }
  }

  // devuelve null si no es posible inscribirse al evento o el estado del mismo
  async checkStatusEventToInscript(): Promise<string | null> {
    if (!this.event?.id) return null;

    try {
      const data = await firstValueFrom(
        this.eventService.getStatusEventById("" + this.event.id)
      );

      const dataStatus: DataStatusEvent = data.data as DataStatusEvent;
      console.log('[estadoEvento] => estado del evento: ', dataStatus);

      if (dataStatus.status === StatusEvent.OPEN) {
        // if (this.event.eventType === EventTypes.GIVEAWAY) {
        //   this.setOpenModalInscript(true);
        // }
        // if (this.event.eventType === EventTypes.RAFFLES) {
        //   this.setOpenModalRaffle(true);
        // }
        this.event.eventType === EventTypes.RAFFLES ? this.setOpenModalRaffle(true) : this.setOpenModalInscript(true);
      } else {
        this.event.statusEvent = dataStatus.status as StatusEvent;
      }

      return this.event.statusEvent;
    } catch (err) {
      console.error('Error al obtener el estado del evento:', err);
      return null;
    }
  }


  // setea los numeros a comprar y habilita la compra de los mismos
  toBuyNumbersRaffle(numbers: number[]) {
    this.selectedRaffleNumbers = numbers;
    // console.log('[buyNumbers] => Numeros por comprar: ' + this.selectedRaffleNumbers);
    this.setOpenModalRaffle(false);
    this.setOpenModalInscript(true);
  }

  // recupera los datos necesarios para realizar la compra
  getDataPayment(user: UserDTO, numbers: number[]) {
    const operator = this.authService.getCurrentUserValue();
    if (this.event) {
      const ammount = this.selectedRaffleNumbers.length * this.event.priceOfNumber;
      const data = {
        fristName: user.name,
        lastName: user.surname,
        idUser: operator ? operator.id : null,
        idEvent: "" + this.event.id,
        email: user.email,
        phone: user.cellphone ? user.cellphone : "",
        ammount: ammount,
        numbers: numbers
      }
      return data as DataPayment;
    }
    return null;
  }
}