import { Component } from '@angular/core';
import { MpBrick } from '../../shared/components/mp-brick/mp-brick';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { EventsTemp } from '../../models/events.model';
import { NotificationService } from '../../services/notification.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-payments',
  imports: [CommonModule, MpBrick],
  templateUrl: './payments.html',
  styleUrl: './payments.css'
})
export class Payments {

  eventCurrent!:EventsTemp|null;
  successfulPayment: boolean = false;

  constructor(
    private adminEventService: AdminEventService,
    private notificationService: NotificationService,
    private router: Router
  ){
    this.adminEventService.selectedEvent$.subscribe(
      event => {
        this.eventCurrent = event;
      }
    )
  }

  // tratamos el resultado del pago
  onFinalizedPay(data: any){
    if(data.success){
      this.successfulPayment = true;
      this.notificationService.notifySuccess("La compra fue realizada con éxito")
    }
    else{
      this.notificationService.notifyError("No se ha podido realizar el apgo correctamente!");
    }
  }

  // manejamos la redireccion
  selectRedirect(paramRedirect: string){
    if(paramRedirect == "details"){
      this.router.navigate(['/event/management/'+this.eventCurrent?.id]);
    }
    if(paramRedirect == "hub"){
      this.router.navigate(['/draws/all']);
    }
  }

}
