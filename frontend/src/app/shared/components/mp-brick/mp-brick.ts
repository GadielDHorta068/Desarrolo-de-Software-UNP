import { Component, EventEmitter, Output, signal } from '@angular/core';
import { LoadingIndicator } from '../loading-indicator/loading-indicator';
import { MercadoPagoService } from '../../../services/mercado-pago.service';
import { AdminPaymentService, DataPayment } from '../../../services/admin/adminPayment.service';

declare var MercadoPago: any;

@Component({
  selector: 'app-mp-brick',
  imports: [LoadingIndicator],
  templateUrl: './mp-brick.html',
  styleUrl: './mp-brick.css'
})
export class MpBrick {
  // la pkey del venderor
  // private publicKey = 'TEST-3978631b-9a9f-4071-a497-792d8d0cad9d';
  private publicKey = 'TEST-fc380a7a-2c59-4119-b31e-00e8eac0ff9d';
  dataPayment!: DataPayment|null;
  isPaymentLoading = false;

  @Output() resultPay = new EventEmitter<any>();

  constructor(
    private mercadopagoService: MercadoPagoService,
    private adminPaymentService: AdminPaymentService
  ){
    this.adminPaymentService.dataPayment$.subscribe(
      data => {
        this.dataPayment = data;
        // console.log("[mpBrick] => datos del pago: ", this.dataPayment);
      }
    )
  }

  ngOnInit(): void {
    const mp = new MercadoPago(this.publicKey, { locale: 'es-AR' });
    const bricksBuilder = mp.bricks();

    bricksBuilder.create('payment', 'card-payment-container', {
      initialization: {
        amount: this.dataPayment?.ammount,
        // preferenceId: "501812522-16cfb9bd-6830-41ef-a166-033a6fd7e9df",
        preferenceId: "",
        payer: {
          firstName: this.dataPayment?.fristName,
          lastName: this.dataPayment?.lastName,
          // email: "jhon@doe.com",
          email: this.dataPayment?.email,
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
          this.isPaymentLoading = true;
          this.payMp(formData).subscribe({
            next: (data) => {
              console.log('[pagoMP] => datos del pago: ', data);
              this.resultPay.emit(data);
            },
            error: (err) => {
              console.error('[pagoMP] => Error al realizar el pago: ', err);
              this.resultPay.emit(err);
            },
            complete: () => {
              this.isPaymentLoading = false;
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

    const bodyPayment = {
      // en el caso de no estar logueado se debe poder asociar el 
      paymentData: {
        idUser: this.dataPayment?.idUser,
        mailUser: this.dataPayment?.email,
        idEvent: this.dataPayment?.idEvent,
        numbers: this.dataPayment?.numbers
      },
      paymentMp: paymentData
    }
    // return this.mercadopagoService.reportPayment(paymentData);
    return this.mercadopagoService.reportPayment(bodyPayment);
  }
}
