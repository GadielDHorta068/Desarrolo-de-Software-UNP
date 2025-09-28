import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventsCreate, EventsTemp, EventTypes } from '../../models/events.model';
import { Category } from '../../services/category.service';
import { configService } from '../../services/config.service';
import { EventsService } from '../../services/events.service';
import { AuthService, UserResponse } from '../../services/auth.service';
import { InfoModal, ModalInfo } from '../../shared/components/modal-info/modal-info';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';

@Component({
  selector: 'app-raffles-panel',
  imports: [CommonModule, ReactiveFormsModule, ModalInfo, LoaderImage],
  templateUrl: './raffles-panel.html',
  styleUrl: './raffles-panel.css',
  standalone: true
})
export class RafflesPanel {

  @ViewChild('modalInfo') modalInfoRef!: ModalInfo;

  formPanel: FormGroup;
  userCurrent: UserResponse|null = null;
  dataModal: InfoModal = {title: "Creación de eventos", message: ""};

  // tipos de sorteo
  types: {code: string, name: string}[] = [
    { code: EventTypes.RAFFLES, name: 'Rifa' },
    { code: EventTypes.GIVEAWAY, name: 'Sorteo' },
  ];

  categories: Category[] = [];

  constructor(
    private configService: configService,
    private eventService: EventsService,
    private authService: AuthService
  ){
    this.userCurrent = this.authService.getCurrentUserValue();
    // console.log("[createEvent] => usuario actual: ", this.userCurrent);
    this.categories = this.configService.getAllCategories();

    // inicializacion del form de creacion de eventos
    this.formPanel = new FormGroup({
      title: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      category: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      executionDate: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      winners: new FormControl({value: 1, disabled: false}, {validators:[ Validators.required ]}),
      description: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
    });
  }

  public createDraw(){
    /* this.checkStatusControl();
    return; */
    // console.log("[crearSorteo] => datos del sorteo: ", this.formPanel.value);
    const dataNewEvent = this.getNewEvent(this.formPanel.value);
    // console.log("[crearSorteo] => datos del sorteo parseado: ", dataNewEvent);
    this.eventService.createEvent(""+this.userCurrent?.id, dataNewEvent).subscribe({
      next: (response) => {
          // console.log('[initConfig] => nuevo evento creado: ', response);
        this.dataModal.message = "Evento creado correctamente";
        this.modalInfoRef.open();
      },
      error: (error) => {
          console.warn('[Eventos]: error al crear el evento: ', error);
          this.dataModal.message = "Error al crear el evento. ", error.error;
          // NOTA: cuando la fecha esta errada no es un json la respuesta, corregir
          this.modalInfoRef.open();
        // });
      }
    });
  }

  // para test interno
  // private checkStatusControl(){    
  //   console.log("[validForm] => titulo: ", this.formPanel.get('title')?.valid);
  //   console.log("[validForm] => description: ", this.formPanel.get('description')?.valid);
  //   console.log("[validForm] => drawType: ", this.formPanel.get('drawType')?.valid);
  //   console.log("[validForm] => category: ", this.formPanel.get('category')?.valid);
  //   console.log("[validForm] => executionDate: ", this.formPanel.get('executionDate')?.valid);
  //   console.log("[validForm] => winners: ", this.formPanel.get('winners')?.valid);
  //   console.log("[validForm] => winners value: ", this.formPanel.get('winners')?.value);
  //   console.log("[validForm] => winners erros: ", this.formPanel.get('winners')?.errors);
  // }

  private getNewEvent(dataEvent: any){
    return {
      title: dataEvent.title,
      description: dataEvent.description,
      eventType: dataEvent.drawType,
      category: {
        id: Number(dataEvent.category)
      },
      endDate: dataEvent.executionDate,
      winnersCount: dataEvent.winners
    } as EventsCreate;
  }

  // TODO: falta el reste del form luego de crear un soreo con exito
  // TODO: se podria cambiar el color de fondo del modal o del titulo segun el tipo de response

}
