import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Events, EventsTemp, EventType, EventTypes } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { configService } from '../../services/config.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';

@Component({
  selector: 'app-edit-event',
  imports: [CommonModule, ReactiveFormsModule, LoaderImage],
  templateUrl: './edit-event.html',
  styleUrl: './edit-event.css',
  providers: [HandleDatePipe]
})
export class EditEvent {

  // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
  // event!: Events|null;
  event!: EventsTemp|null;

  formEvent: FormGroup;
  
  // tipos de sorteo
  // types: {code: string, name: string}[] = [
  //   { code: EventTypes.RAFFLES, name: 'Rifa' },
  //   { code: EventTypes.GIVEAWAY, name: 'Sorteo' },
  // ];
  types: EventType[] = [];

  // categorias de sorteo
  categories: Category[] = [];

  constructor(
    private adminEventService: AdminEventService,
    private configService: configService,
    private datePipe: HandleDatePipe
  ){
    // FormGroup({
    //   valueCivilId: new FormControl({ value: civilIdFormatted, disabled: false}, {
    //             validators:[ Validators.required ]

    this.formEvent = new FormGroup({
      title: new FormControl({value: this.event?.title, disabled: true}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: this.event?.eventType, disabled: true}, {validators:[ Validators.required ]}),
      category: new FormControl({value: this.event?.categoryName, disabled: false}),
      executionDate: new FormControl({value: this.event?.endDate, disabled: false}, {validators:[ Validators.required ]}),
      winners: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      description: new FormControl({value: this.event?.description, disabled: false}, {validators:[ Validators.required ]}),
    });

    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.event = currentEvent;
        console.log("[edicion] => evento seleccionado: ", this.event);
        this.updateForm();
      }
    )

    this.categories = this.configService.getCategories();
    this.types = this.configService.getEventTypes();
  }

  public onSaveChanges(){
    console.log("[crearSorteo] => datos del sorteo: ", this.formEvent.value);
  }


  private updateForm(){
    this.formEvent.get('title')?.setValue(this.event?.title);
    this.formEvent.get('drawType')?.setValue(this.event?.eventType);
    this.formEvent.get('category')?.setValue(this.event?.categoryId);
    // this.formEvent.get('executionDate')?.setValue(this.event?.endDate);
    let dateEvent = this.event?.endDate ? this.datePipe.transform(this.event?.endDate): "";
    this.formEvent.get('executionDate')?.setValue(this.parseDate(dateEvent));
    this.formEvent.get('winners')?.setValue(this.event?.winnersCount);
    this.formEvent.get('description')?.setValue(this.event?.description);
  }

  private parseDate(fecha: string): string {
    const [dia, mes, anio] = fecha.split('-');

    const diaFormateado = dia.padStart(2, '0');
    const mesFormateado = mes.padStart(2, '0');

    return `${anio}-${mesFormateado}-${diaFormateado}`;
  }

}
