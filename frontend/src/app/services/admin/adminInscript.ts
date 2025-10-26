import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { AuditService, WinnersAudit } from '../audit.service';
import { UserDTO } from '../../models/UserDTO';
import { BuyRaffleNumberDTO } from '../../models/buyRaffleNumberDTO';
import { QuestionaryService } from '../questionary.service';
import { NotificationService } from '../notification.service';
import { AdminEventService } from './adminEvent.service';
import { EventsService } from '../events.service';
import { DataStatusEvent } from '../../models/response.model';

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

  // evento en contexto
  event!: EventsTemp|null;

  selectedRaffleNumbers: number[] = [];

  constructor(
    private questionaryService: QuestionaryService,
    private notificationService: NotificationService,
    private adminEventService: AdminEventService,
    private eventService: EventsService
  ) {
    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.event = currentEvent;
      }
    )
  }

  // administran la apertura de los modales de inscripcion y seleccion de nros
  setOpenModalInscript(open: boolean){
    this.openInscriptionSubject.next(open);
  }
  setOpenModalRaffle(open: boolean){
    this.openRaffleNUmbersSubject.next(open);
  }

  // inscribe al usuario al evento seleccionado
  // onInscript(user: UserDTO): void {
  //   if (!this.event) return;

  //   if (this.event.eventType === EventTypes.RAFFLES) {
  //     const buyNumRequest: BuyRaffleNumberDTO = {
  //       aGuestUser: user,
  //       someNumbersToBuy: this.selectedRaffleNumbers
  //     }
  //     this.questionaryService.saveRaffleNumber(
  //       this.event.id,
  //       buyNumRequest
  //     ).subscribe({
  //       next: (response) => {
  //         this.notificationService.notifySuccess(response.message);
  //         this.cdr.detectChanges();
  //       },
  //       error: (errorResponse) => {
  //         console.log('[adminInscript] => error al comprar los nros!');
  //         this.notificationService.notifyError(errorResponse.error.message);
  //         this.cdr.detectChanges();
  //       }
  //     });
  //   }
  //   else {
  //     this.questionaryService.save(
  //       user,
  //       this.event.id
  //     ).subscribe({
  //       next: (response) => {
  //         this.notificationService.notifySuccess(response.message);
  //       },
  //       error: (errorResponse) => {
  //         console.log('[adminInscript] => Error al comprar los nueros:', JSON.stringify(errorResponse)); // borrar
  //         this.notificationService.notifyError(errorResponse.error.message);
  //       }
  //     });
  //   }
  // }

  async onInscript(user: UserDTO): Promise<void> {
    if (!this.event) return;

    try {
      if (this.event.eventType === EventTypes.RAFFLES) {
        const buyNumRequest: BuyRaffleNumberDTO = {
          aGuestUser: user,
          someNumbersToBuy: this.selectedRaffleNumbers
        };
        const response = await firstValueFrom(
          this.questionaryService.saveRaffleNumber(this.event.id, buyNumRequest)
        )
        return response;
      } else {
        const response = await firstValueFrom(
          this.questionaryService.save(user, this.event.id)
        );
        return response;
      }
    } catch (errorResponse: any) {
      console.log('[adminInscript] => Error al comprar los números:', JSON.stringify(errorResponse));
      // this.notificationService.notifyError(errorResponse.error.message);
      // let dataResponse = {"data":null,"message":errorResponse.error.message,"status":400};
      return Promise.resolve(errorResponse);
    }
  }


  // controla el estado del evento y maneja la apertura de los modal de compra de nros o de inscripcion
  // checkStatusEventToInscript(): void{
  //   this.eventService.getStatusEventById(""+this.event?.id).subscribe({
  //       next: (data) => {
  //           console.log('[estadoEvento] => estado del evento: ', data);
  //           const dataStatus: DataStatusEvent = data.data as DataStatusEvent;
  //           // this.dataModal.message = "Estado del evento: ", dataStatus.status;
  //           if(dataStatus.status === StatusEvent.OPEN){
  //               // TODO: aca permitimos la inscripcion    
  //               if (this.event?.id && this.event?.eventType === EventTypes.GIVEAWAY) {
  //                   // mostramos el form de inscripcion al sorteo
  //                   this.setOpenModalInscript(true);
  //               }
  //               if (this.event?.id && this.event?.eventType === EventTypes.RAFFLES) {
  //                   try {
  //                       this.setOpenModalRaffle(true);
  //                   } catch (err) {
  //                       console.error('ERROR dentro de onInscript (bloque RAFFLE):', err);
  //                   }
  //               }
  //           }
  //           else{
  //               if(this.event){
  //                   this.event.statusEvent = dataStatus.status as StatusEvent
  //               }
  //           }
  //       },
  //       error: (err) => {
  //           console.error('Error al obtener el estado del evento:', err);
  //       }
  //   })
  // }
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
        if (this.event.eventType === EventTypes.GIVEAWAY) {
          this.setOpenModalInscript(true);
        }
        if (this.event.eventType === EventTypes.RAFFLES) {
          this.setOpenModalRaffle(true);
        }
        return "OK";
      } else {
        this.event.statusEvent = dataStatus.status as StatusEvent;
        return this.event.statusEvent;
      }

      return dataStatus.status;
    } catch (err) {
      console.error('Error al obtener el estado del evento:', err);
      return null;
    }
  }

  
  // setea los numeros a comprar y habilita la compra de los mismos
  toBuyNumbersRaffle(numbers: number[]){
    this.selectedRaffleNumbers = numbers;
    console.log('[buyNumbers] => Numeros por comprar: ' + this.selectedRaffleNumbers);
    this.setOpenModalRaffle(false);
    this.setOpenModalInscript(true);
  }
}