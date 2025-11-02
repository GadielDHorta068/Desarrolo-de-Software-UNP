import { Injectable } from '@angular/core';
import { BehaviorSubject} from 'rxjs';

export interface DataPayment {
  fristName: string,
  lastName: string,
  idUser: string|null,
  idEvent: string,
  email: string,
  phone: string,
  ammount: number
}

@Injectable({
  providedIn: 'root'
})
export class AdminPaymentService {

  // datos del pago
  private dataPaymentSubject = new BehaviorSubject<DataPayment|null>(null);
  public dataPayment$ = this.dataPaymentSubject.asObservable();

  constructor() {}

  // administran la informacion asociada a la compra
  setDataPayment(data: DataPayment|null){
    this.dataPaymentSubject.next(data);
  }

}