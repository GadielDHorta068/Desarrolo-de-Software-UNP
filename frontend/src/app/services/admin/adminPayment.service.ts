import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface DataPayment {
  fristName: string,
  lastName: string,
  idUser: string | null,
  idEvent: string,
  email: string,
  phone: string,
  ammount: number,
  numbers: number[]
}

@Injectable({
  providedIn: 'root'
})
export class AdminPaymentService {

  // datos del pago
  private dataPaymentSubject = new BehaviorSubject<DataPayment | null>(null);
  public dataPayment$ = this.dataPaymentSubject.asObservable();

  constructor() {
    const savedData = localStorage.getItem('dataPayment');
    if (savedData) {
      try {
        this.dataPaymentSubject.next(JSON.parse(savedData));
      } catch (e) {
        console.error('Error parsing dataPayment from localStorage', e);
        localStorage.removeItem('dataPayment');
      }
    }
  }

  // administran la informacion asociada a la compra
  setDataPayment(data: DataPayment | null) {
    this.dataPaymentSubject.next(data);
    if (data) {
      localStorage.setItem('dataPayment', JSON.stringify(data));
    } else {
      localStorage.removeItem('dataPayment');
    }
  }

}