import { ChangeDetectorRef, Component } from '@angular/core';
import { Events, EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../services/events.service';
import { AuthService, UserResponse } from '../../services/auth.service';
import { Router } from '@angular/router';
// import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-panel-list',
  imports: [CommonModule, FormsModule, DrawCard],
  templateUrl: './panel-list.html',
  styleUrl: './panel-list.css'
})
export class PanelList {

  userCurrent: UserResponse|null = null;
  totalEvents: EventsTemp[] = [];
  events: EventsTemp[] = [];

  constructor(
    private eventService: EventsService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ){}

  ngOnInit(): void {
    this.userCurrent = this.authService.getCurrentUserValue();
    this.getDraws();
  } 

  // selectedOption?: string;

  // se filtra en la lista recuperada inicialmente
  public aplyFilter(filter: string|null){
    // console.log("[radioButton] => filtro seleccionado: ", filter);
    // this.events = filter ? this.totalEvents.filter((evt: Events) => evt.statusEvent == filter) : this.totalEvents;
    this.events = filter ? this.totalEvents.filter((evt) => evt.statusEvent == filter) : this.totalEvents;
  }

  public onRedirectToCreate(){
    this.router.navigate(['/event/new']);
  }

  private getDraws(){
    // Verificar que hay un usuario autenticado antes de hacer la llamada
    if (!this.userCurrent || !this.userCurrent.id) {
      console.warn('[Eventos]: No hay usuario autenticado para cargar eventos');
      this.router.navigate(['/login']); // Redirigir al login si no hay usuario
      return;
    }

    this.eventService.getAllByCreator(this.userCurrent.id.toString()).subscribe({
        next: (response) => {
          // this.router.navigate(['/home']);
          console.log('[Eventos]: eventos recuperados: ', response);
          this.totalEvents = response;
          this.events = this.totalEvents;
          this.cdr.detectChanges();     // forzamos la actualizacion de la vista
        },
        error: (error) => {
          // Usar el mensaje específico del backend si está disponible
          // this.errorMessage = error.userMessage || 'Error al registrar usuario. Por favor, intenta de nuevo.';
          console.warn('[Eventos]: error al recuperar los eventos: ', error);
        }
      });
  }
}
