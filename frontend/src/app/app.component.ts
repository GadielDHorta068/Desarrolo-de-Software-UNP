import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, ChildrenOutletContexts, Router, RouterLink } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingIndicator } from './shared/components/loading-indicator/loading-indicator';
import { slideInAnimation } from './animations/route-animations';
import { QuestionaryComponent } from './pages/questionary/questionary.component';
import { RaffleNumbersComponent } from './pages/raffle-numbers.component/raffle-numbers.component';
import { CommonModule } from '@angular/common';
import { AdminInscriptService } from './services/admin/adminInscript';
import { NotificationService } from './services/notification.service';
import { UserDTO } from './models/UserDTO';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, HeaderComponent, QuestionaryComponent, RaffleNumbersComponent, LoadingIndicator],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [slideInAnimation],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  // Componente principal de la aplicación
  // Utiliza OnPush para mejor rendimiento

  showModalInscript: boolean = false;
  showRaffleModal: boolean = false;
  redirectingToPayment: boolean = false;
  
  constructor(
    private contexts: ChildrenOutletContexts,
    private cdr: ChangeDetectorRef,
    private adminInscriptService: AdminInscriptService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    // Removido initData() - ahora se llama solo en las páginas que lo necesitan
    this.adminInscriptService.openInscription$.subscribe(resp => {
      this.showModalInscript = resp;
      this.cdr.markForCheck();
    })

    this.adminInscriptService.openRaffleNUmbers$.subscribe(resp => {
      this.showRaffleModal = resp;
      this.cdr.markForCheck();
    })
  }
  
  openModalRaffle(){
    this.showRaffleModal = !this.showRaffleModal;
  }
  openModalInscript(){
    this.showModalInscript = !this.showModalInscript;
  }

  onRaffleClosed(){
    this.adminInscriptService.setOpenModalRaffle(false);
  }
  onInscriptClosed(){
    this.adminInscriptService.setOpenModalInscript(false);
  }

  async onQuestionarySubmit(data: UserDTO){
    this.redirectingToPayment = true;
    try {
      const respInscript: any = await this.adminInscriptService.onInscript(data);
      if(respInscript.status == 200){
        if(respInscript.redirectPay){
          this.router.navigate(['/event/payment']);
        }
        else{
          this.notificationService.notifySuccess(respInscript.message)
        }
      }
      else{
        this.notificationService.notifyError("Ha ocurrido un error en la inscripcion al evento.");
      }
    } finally {
      this.redirectingToPayment = false;
      this.cdr.markForCheck();
    }
  }
  
  onProceedToQuestionary(data: any){
    this.adminInscriptService.toBuyNumbersRaffle(data);
  }

  getRouteAnimationData() {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
  }
}
