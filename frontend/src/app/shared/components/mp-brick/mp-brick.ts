import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Output, signal } from '@angular/core';

declare var MercadoPago: any;

@Component({
  selector: 'app-mp-brick',
  imports: [],
  templateUrl: './mp-brick.html',
  styleUrl: './mp-brick.css'
})
export class MpBrick {
  // la pkey del venderor
  // private publicKey = 'TEST-3978631b-9a9f-4071-a497-792d8d0cad9d';
  private publicKey = 'TEST-fc380a7a-2c59-4119-b31e-00e8eac0ff9d';
  private amount = signal(1000); // Reactive amount
  // private excludedTypes = signal<string[]>(['debit_card']); // Reactive exclusion
  private excludedTypes = signal<string[]>([]); // Reactive exclusion

  @Output() resultPay = new EventEmitter<any>();

  constructor(
    private httpClient: HttpClient
  ){

  }

  ngOnInit(): void {
    const mp = new MercadoPago(this.publicKey, { locale: 'es-AR' });
    const bricksBuilder = mp.bricks();

    bricksBuilder.create('payment', 'card-payment-container', {
      initialization: {
        amount: 100,
        // preferenceId: "501812522-16cfb9bd-6830-41ef-a166-033a6fd7e9df",
        preferenceId: "",
        payer: {
          firstName: "",
          lastName: "",
          email: "jhon@doe.com",
        },
      },
      customization: {
        visual: {
          style: {
            theme: "dark",
          },
        },
        paymentMethods: {
          wallet_purchase: "all",
          debitCard: "all",
          ticket: "all",
          bankTransfer: "all",
          onboarding_credits: "all",
          maxInstallments: 1
        },
        // paymentMethods: {
        //   ticket: 'all',
        //   // credit_card: 'all',
        //   debit_card: 'all',
        // },
      },
      callbacks: {
        onReady: () => {
          /*
           Callback llamado cuando el Brick está listo.
           Aquí puede ocultar cargamentos de su sitio, por ejemplo.
          */
        },
        onSubmit: ({ selectedPaymentMethod, formData }: any) => {
        // onSubmit: (formData: any) => {
          this.payMp(formData).subscribe({
            next: (data) => {
              console.log('[pagoMP] => datos del pago: ', data);
              this.resultPay.emit(data);
            },
            error: (err) => {
              console.error('[pagoMP] => Error al realizar el pago: ', err);
              this.resultPay.emit(err);
            }
          })
          // console.log("[pagoMP] => datos del form de pago: ", formData);
        },
        onError: (error: any) => {
          // callback llamado para todos los casos de error de Brick
          console.error(error);
        },
      },
    });
  }

  payMp(formData: any){
    console.log("[pagoMP] => datos del form de pago: ", formData);
    // const urlPayment = "http://localhost:3000/process_payment";
    const urlPayment = "http://localhost:808/mp/process_payment";
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer TEST-911685102344613-102107-142a437bbcd8bb76c3281eae04eeffb9-501812522`
    });

    const paymentData = {
      transaction_amount: formData.transaction_amount,
      token: formData.token,
      description: formData.description,
      installments: formData.installments,
      payment_method_id: formData.payment_method_id,
      issuer_id: formData.issuer_id,
      payer: {
        email: formData.payer.email,
        identification: {
          type: formData.payer.identification.type,
          number: formData.payer.identification.number,
        },
      },
    };
    
    const bodyFormData = {
      formData: paymentData
    }

    // return this.httpClient.post(urlPayment, paymentData, {headers})
    return this.httpClient.post(urlPayment, JSON.stringify(bodyFormData), {headers})
  }
}
