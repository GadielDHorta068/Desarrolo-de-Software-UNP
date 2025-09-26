import { ChangeDetectorRef, Component } from '@angular/core';
import { Events, EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { DrawCard } from '../../shared/components/draw-card/draw-card';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../services/events.service';
// import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-panel-list',
  imports: [CommonModule, FormsModule, DrawCard],
  templateUrl: './panel-list.html',
  styleUrl: './panel-list.css'
})
export class PanelList {

  // totalEvents: Events[] = [
  //   {
  //     id: 123,
  //     title: 'Sorteo de Tablet Samsung',
  //     endDate: '2025-09-20',
  //     description: "Aca iria una descripcion algo larga del evento que posiblemente abarque todo el ancho",
  //     startDate: "fecha de incio",
  //     category: {id: 1, name: "deporte"},
  //     statusEvent: StatusEvent.ACTIVE,
  //     eventType: EventTypes.CONTEST,
  //     winnersCount: 1
  //   },
  //   {
  //     id: 12,
  //     title: 'Sorteo de Auriculares JBL',
  //     endDate: '2025-09-25',
  //     description: "la descripcion 2",
  //     startDate: "fecha de incio",
  //     category: {id: 1, name: "deporte"},
  //     statusEvent: StatusEvent.BLOCKED,
  //     eventType: EventTypes.GIVEAWAY,
  //     winnersCount: 1
  //   },
  //   {
  //     id: 11,
  //     title: 'Sorteo Gift Card $1000',
  //     endDate: '2025-09-30',
  //     description: "la descripcion 3",
  //     startDate: "fecha de incio",
  //     category: {id: 1, name: "escolar"},
  //     statusEvent: StatusEvent.FINISHED,
  //     eventType: EventTypes.GIVEAWAY,
  //     winnersCount: 1
  //   },
  //   {
  //     id: 983,
  //     title: 'Sorteo de Bicicleta Eléctrica',
  //     endDate: '2025-10-05',
  //     description: "la descripcion 4",
  //     startDate: "fecha de incio",
  //     category: {id: 1, name: "deporte"},
  //     statusEvent: StatusEvent.BLOCKED,
  //     eventType: EventTypes.GIVEAWAY,
  //     winnersCount: 1
  //   },
  //   {
  //     id: 762,
  //     title: 'Sorteo de Smartwatch',
  //     endDate: '2025-10-10',
  //     description: "la descripcion 5",
  //     startDate: "fecha de incio",
  //     category: {id: 1, name: "deporte"},
  //     statusEvent: StatusEvent.CLOSED,
  //     eventType: EventTypes.TOURNAMENT,
  //     winnersCount: 1
  //   }
  // ];

  // events: Events[] = [];

  totalEvents: EventsTemp[] = [];
  events: EventsTemp[] = [];

  constructor(
    private eventService: EventsService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    // this.events = this.totalEvents;
    this.getDraws();
  } 

  // selectedOption?: string;

  // se filtra en la lista recuperada inicialmente
  public aplyFilter(filter: string|null){
    // console.log("[radioButton] => filtro seleccionado: ", filter);
    // this.events = filter ? this.totalEvents.filter((evt: Events) => evt.statusEvent == filter) : this.totalEvents;
    this.events = filter ? this.totalEvents.filter((evt) => evt.statusEvent == filter) : this.totalEvents;
  }

  private getDraws(){
    this.eventService.getAllEvents().subscribe({
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
