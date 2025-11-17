import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, ChildrenOutletContexts, Router } from '@angular/router';
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
  imports: [CommonModule, RouterOutlet, HeaderComponent, QuestionaryComponent, RaffleNumbersComponent, LoadingIndicator],
  template: `
    <main class="min-h-screen flex flex-col font-sans bg-gray-50 dark:bg-gray-900">
      <header class="bg-gradient-to-r from-indigo-500 to-purple-600 dark:from-indigo-600 dark:to-purple-700 text-white shadow-lg">
        <app-header></app-header>
      </header>

      <section class="flex-1 bg-gray-50 dark:bg-gray-900 overflow-hidden" [@routeAnimations]="getRouteAnimationData()">
        <router-outlet />
      </section>
      
<!-- Footer -->
    <footer class="bg-gray-900 text-white py-12">
      <div class="container mx-auto px-6">
        <div class="grid md:grid-cols-4 gap-8">
          <div>
            <h3 class="text-2xl font-bold mb-4 text-gradient bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              RAFFIFY
            </h3>
            <p class="text-gray-400 mb-4">
              La plataforma líder en sorteos y rifas digitales con transparencia blockchain.
            </p>
            <div class="flex space-x-4">
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/>
                </svg>
              </a>
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M22.46 6c-.77.35-1.6.58-2.46.69.88-.53 1.56-1.37 1.88-2.38-.83.5-1.75.85-2.72 1.05C18.37 4.5 17.26 4 16 4c-2.35 0-4.27 1.92-4.27 4.29 0 .34.04.67.11.98C8.28 9.09 5.11 7.38 3 4.79c-.37.63-.58 1.37-.58 2.15 0 1.49.75 2.81 1.91 3.56-.71 0-1.37-.2-1.95-.5v.03c0 2.08 1.48 3.82 3.44 4.21a4.22 4.22 0 0 1-1.93.07 4.28 4.28 0 0 0 4 2.98 8.521 8.521 0 0 1-5.33 1.84c-.34 0-.68-.02-1.02-.06C3.44 20.29 5.7 21 8.12 21 16 21 20.33 14.46 20.33 8.79c0-.19 0-.37-.01-.56.84-.6 1.56-1.36 2.14-2.23z"/>
                </svg>
              </a>
              <a href="#" class="text-gray-400 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12.017 0C5.396 0 .029 5.367.029 11.987c0 5.079 3.158 9.417 7.618 11.174-.105-.949-.199-2.403.041-3.439.219-.937 1.406-5.957 1.406-5.957s-.359-.72-.359-1.781c0-1.663.967-2.911 2.168-2.911 1.024 0 1.518.769 1.518 1.688 0 1.029-.653 2.567-.992 3.992-.285 1.193.6 2.165 1.775 2.165 2.128 0 3.768-2.245 3.768-5.487 0-2.861-2.063-4.869-5.008-4.869-3.41 0-5.409 2.562-5.409 5.199 0 1.033.394 2.143.889 2.741.099.12.112.225.085.345-.09.375-.293 1.199-.334 1.363-.053.225-.172.271-.402.165-1.495-.69-2.433-2.878-2.433-4.646 0-3.776 2.748-7.252 7.92-7.252 4.158 0 7.392 2.967 7.392 6.923 0 4.135-2.607 7.462-6.233 7.462-1.214 0-2.357-.629-2.75-1.378l-.748 2.853c-.271 1.043-1.002 2.35-1.492 3.146C9.57 23.812 10.763 24.009 12.017 24.009c6.624 0 11.99-5.367 11.99-11.988C24.007 5.367 18.641.001 12.017.001z"/>
                </svg>
              </a>
            </div>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Producto</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Características</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Precios</a></li>
              <li><a href="#" class="hover:text-white transition-colors">API</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Integraciones</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Soporte</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Centro de Ayuda</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Documentación</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Contacto</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Estado del Sistema</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-lg font-semibold mb-4">Legal</h4>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white transition-colors">Términos de Uso</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Política de Privacidad</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Cookies</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Transparencia</a></li>
            </ul>
          </div>
        </div>
        
        <div class="border-t border-gray-800 mt-12 pt-8 text-center text-gray-400">
          <p>&copy; 2025 RAFFIFY. Todos los derechos reservados. Desarrollado con ❤️ para modernizar los sorteos.</p>
        </div>
      </div>
    </footer>
    </main>

    <div class="fixed inset-0 z-50" *ngIf="redirectingToPayment">
      <app-loading-indicator [active]="true" [type]="'overlay'" [message]="'Redirigiendo al pago...'" [size]="'md'"></app-loading-indicator>
    </div>

    <app-questionary
        *ngIf="showModalInscript"
        (close)="onInscriptClosed()"
        (onInscript) = "onQuestionarySubmit($event)"
    ></app-questionary>

    <app-raffle-numbers
        *ngIf = "showRaffleModal"
        (closeRaffleNumberModal) = "onRaffleClosed()"
        (proceedToInscript)="onProceedToQuestionary($event)"
    ></app-raffle-numbers>
  `,
  styles: [],
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
