import { Component, OnInit } from '@angular/core';
import { MpBrick } from '../../shared/components/mp-brick/mp-brick';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsTemp } from '../../models/events.model';
import { NotificationService } from '../../services/notification.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AdminPaymentService } from '../../services/admin/adminPayment.service';

@Component({
  selector: 'app-payments',
  imports: [CommonModule, MpBrick],
  templateUrl: './payments.html',
  styleUrl: './payments.css'
})
export class Payments implements OnInit {

  eventCurrent!: EventsTemp | null;
  successfulPayment: boolean = false;

  constructor(
    private adminEventService: AdminEventService,
    private adminPaymentService: AdminPaymentService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.adminEventService.selectedEvent$.subscribe(
      event => {
        this.eventCurrent = event;
      }
    )
  }

  ngOnInit(): void {
    // Check if there is payment data, otherwise redirect
    // We need to take 1 or check synchronously to avoid redirecting while loading?
    // BehaviorSubject always has a value. If it's null (and not successful payment yet), redirect.
    // Note: We subscribe here, so if it changes to null (which we do on success), we need to make sure we don't redirect if successfulPayment is true.
    this.adminPaymentService.dataPayment$.subscribe(data => {
      if (!data && !this.successfulPayment) {
        // Redirect if no data and not currently showing success screen
        this.router.navigate(['/public-events']);
      }
    });
  }

  // tratamos el resultado del pago
  onFinalizedPay(data: any) {
    // console.log("[dataPayServer] => datos: ", data);
    if (data.success || data.data.status == "approved") {
      this.successfulPayment = true;
      this.notificationService.notifySuccess("La compra fue realizada con éxito")
      // Clear payment data to prevent re-payment on refresh
      this.adminPaymentService.setDataPayment(null);
    }
    else {
      // TODO: aca deberia de liberar los nros seleccionados
      this.notificationService.notifyError("No se ha podido realizar el pago correctamente");
    }
  }

  // manejamos la redireccion
  selectRedirect(paramRedirect: string) {
    if (paramRedirect == "details") {
      this.router.navigate(['/event/management/' + this.eventCurrent?.id]);
    }
    if (paramRedirect == "hub") {
      this.router.navigate(['/public-events']);
    }
  }

}
