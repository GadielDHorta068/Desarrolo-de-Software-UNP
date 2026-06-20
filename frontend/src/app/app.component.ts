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
import { EventTypes } from './models/events.model';
import { Guessprogress } from './pages/guessprogress/guessprogress';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, HeaderComponent, QuestionaryComponent, RaffleNumbersComponent, LoadingIndicator, Guessprogress],
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
  showGuessModal: boolean = false;
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

    this.adminInscriptService.openGuessing$.subscribe(resp => {
      this.showGuessModal = resp;
      this.cdr.markForCheck();
    })
  }

  openModalRaffle() {
    this.showRaffleModal = !this.showRaffleModal;
  }
  openModalInscript() {
    this.showModalInscript = !this.showModalInscript;
  }

  openModalGuess() {
    this.showGuessModal = !this.showGuessModal;
    this.cdr.detectChanges();
  }

  onRaffleClosed() {
    this.adminInscriptService.setOpenModalRaffle(false);
  }
  onInscriptClosed() {
    this.adminInscriptService.setOpenModalInscript(false);
  }

  onGuessClosed() {
    this.adminInscriptService.setOpenModalGuessing(false);
  }

    async onQuestionarySubmit(data: UserDTO) {
        this.redirectingToPayment = true;

        try {
            const resp: any = await this.adminInscriptService.onInscript(data);

            // 1️⃣ Error genérico
            if (!resp || resp.status !== 200) {
            this.notificationService.notifyError(
                "Ha ocurrido un error en la inscripción al evento."
            );
            return;
            }

            // 2️⃣ Región (bloqueante total)
            if (resp.data === false) {
            this.notificationService.notifyError(resp.message);
            return;
            }

            // 3️⃣ Rifa → redirección
            if (resp.redirectPay) {
            this.router.navigate(['/event/payment']);
            return;
            }

            // 4️⃣ Guessing contest
            if (resp.isGuessing) {
            if (!resp.allowed) {
                this.notificationService.notifyError(resp.message);
                return;
            }

            this.notificationService.notifySuccess(
                resp.message + ". ¡A jugar!"
            );
            this.adminInscriptService.setOpenModalGuessing(true);
            return;
            }

            // 5️⃣ Caso normal
            this.notificationService.notifySuccess(
            resp.message + ". ¡Gracias por inscribirse!"
            );

        } finally {
            this.redirectingToPayment = false;
            this.cdr.markForCheck();
        }
        }


  onProceedToQuestionary(data: any) {
    this.adminInscriptService.toBuyNumbersRaffle(data);
  }

  getRouteAnimationData() {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
  }
}
