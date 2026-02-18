import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Events, EventsCreate, EventsTemp, EventType, EventTypes } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { configService } from '../../services/config.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { EventsService } from '../../services/events.service';
import { ParseFileService } from '../../services/utils/parseFile.service';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { NotificationService } from '../../services/notification.service';

export function reviewRangeValidators(lowerKey: string, upperKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    // Si el control aún no está dentro de un FormGroup, no validamos
    if (!control.parent) {
      return null;
    }

    const lower = control.parent.get(lowerKey)?.value;
    const upper = control.parent.get(upperKey)?.value;
    const value = control.value;

    // Si falta algún valor, no validamos aquí
    if (value === null || value === '' || lower === null || upper === null) {
      return null;
    }

    const numero = Number(value);
    const min = Number(lower);
    const max = Number(upper);

    if (isNaN(numero) || isNaN(min) || isNaN(max)) {
      return { notNumber: true };
    }

    if (numero < min || numero > max) {
      return { outOfRange: { min, max } };
    }

    return null; // válido
  };
}

export function lowerRangeValidator(upperKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    // Si el control aún no está dentro de un FormGroup, no validamos
    if (!control.parent) {
      return null;
    }

    const lower = control.value;
    const upper = control.parent.get(upperKey)?.value;

    // Si falta algún valor, no validamos aquí
    if (lower === '' || lower === null || upper === '' || upper === null) {
      return null;
    }

    const min = Number(lower);
    const max = Number(upper);

    if (isNaN(min) || isNaN(max)) {
      return { notNumber: true };
    }

    if (min > max) {
      return { errorRange: true };
    }

    return null; // válido
  };
}

export function upperRangeValidator(lowerKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    // Si el control aún no está dentro de un FormGroup, no validamos
    if (!control.parent) {
      return null;
    }

    const upper = control.value;
    const lower = control.parent.get(lowerKey)?.value;

    // Si falta algún valor, no validamos aquí
    if (lower === '' || lower === null || upper === '' || upper === null) {
      return null;
    }

    const min = Number(lower);
    const max = Number(upper);

    if (isNaN(min) || isNaN(max)) {
      return { notNumber: true };
    }

    if (min > max) {
      return { errorRange: true };
    }

    return null; // válido
  };
}

@Component({
  selector: 'app-edit-event',
  imports: [CommonModule, ReactiveFormsModule, LoaderImage],
  templateUrl: './edit-event.html',
  styleUrl: './edit-event.css',
  providers: [HandleDatePipe]
})
export class EditEvent implements OnInit {

  // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
  // event!: Events|null;
  event!: EventsTemp | null;
  eventAux!: EventsTemp | null;
  imageEvent: File | null = null;
  eventIdParam!: Number | null;
  discardedChanges: boolean = false;
  setImageTrigger$ = new BehaviorSubject<any>(null);

  formEvent!: FormGroup;
  // tipos de sorteo
  types: EventType[] = [];
  // categorias de sorteo
  categories: Category[] = [];
  eventTypes = EventTypes;
  maxAttempts: number = 5;

  constructor(
    private route: ActivatedRoute,
    private adminEventService: AdminEventService,
    private configService: configService,
    private datePipe: HandleDatePipe,
    private eventService: EventsService,
    private parseFileService: ParseFileService,
    private cdr: ChangeDetectorRef,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.configService.initData();
    this.initDataLoadEvent();

    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.eventAux = currentEvent ? { ...currentEvent } : null;
        this.event = currentEvent;
        console.log("[edit-event] => evento seleccionado: ", this.event);
        if (this.event) {
          this.initForm();
          this.cdr.markForCheck();
        }
      }
    )
  }

  ngOnInit() {
    this.eventIdParam = Number(this.route.snapshot.paramMap.get('eventId'));
    // console.log("[edit-event] => ide del evento recibido por param: ", this.eventIdParam);
    // revisamos si los datos del evento ya fueron seteados desde la lista de eventos
    if (!this.event) {
      this.eventService.getEventById("" + this.eventIdParam).subscribe(
        resp => {
          this.event = resp;
          console.log("[edit-event] => info del evento recuperado: ", this.event);
          if (this.event) {
            this.eventAux = { ...this.event };
            this.initForm();
            this.cdr.markForCheck();
          }
        }
      )
    }
  }

  public async onSaveChanges() {
    if (this.imageEvent) {
      this.formEvent.get('image')?.setValue(await this.getB64Image(this.imageEvent));
    }
    // console.log("[crearSorteo] => datos del sorteo completos: ", this.formEvent.getRawValue());
    const dataNewEvent = this.getNewEvent(this.formEvent.getRawValue());
    // console.log("[crearSorteo] => datos del sorteo parseado: ", dataNewEvent);
    // this.eventService.updateGiveaways(dataNewEvent, ""+this.event?.id, this.event?.creator?.id, ).subscribe({
    this.eventService.updateEvents(dataNewEvent, "" + this.event?.id, this.event?.creator?.id,).subscribe({
      next: (response: any) => {
        // console.log('[initConfig] => nuevo evento creado: ', response);
        this.notificationService.notifySuccess("Evento editado correctamente");
        this.adminEventService.setSelectedEvent(response);
        this.router.navigateByUrl("/event/management/" + response.id);
        // this.cleanForm();
      },
      error: (error) => {
        console.warn('[Eventos]: error al editar el evento: ', error);
        // NOTA: cuando la fecha esta errada no es un json la respuesta, corregir
        this.notificationService.notifyError("Ha ocurrido un error al realizar la operación");
      }
    });
  }

  private getNewEvent(dataEvent: any) {
    let event: any = {
      title: dataEvent.title,
      description: dataEvent.description,
      eventType: dataEvent.drawType,
      category: {
        id: Number(dataEvent.category)
      },
      endDate: dataEvent.executionDate,
      winnersCount: dataEvent.winners,
      image: dataEvent.image,
      isPrivate: !!dataEvent.isPrivate,
    };
    if (dataEvent.drawType === EventTypes.RAFFLES) {
      event.priceOfNumber = dataEvent.priceOfNumber;
      event.quantityOfNumbers = dataEvent.quantityOfNumbers;
    }
    if (dataEvent.drawType === EventTypes.GUESSING_CONTEST) {
      event.targetNumber = dataEvent.targetNumber;
      event.maxValue = dataEvent.maxValue;
      event.minValue = dataEvent.minValue;
      event.maxAttempts = dataEvent.maxAttempts;
    }
    return event as EventsCreate;
    // return {
    //   title: dataEvent.title,
    //   description: dataEvent.description,
    //   eventType: dataEvent.drawType,
    //   category: {
    //     id: Number(dataEvent.category)
    //   },
    //   endDate: dataEvent.executionDate,
    //   winnersCount: dataEvent.winners,
    //   image: dataEvent.image,
    //   isPrivate: !!dataEvent.isPrivate,
    //   priceOfNumber: dataEvent.priceOfNumber,
    //   quantityOfNumbers: dataEvent.quantityOfNumbers
    // } as EventsCreate;
  }

  // private updateForm(dataEvent: EventsTemp){
  //   this.formEvent.get('title')?.setValue(this.event?.title);
  //   this.formEvent.get('drawType')?.setValue(this.event?.eventType);
  //   this.formEvent.get('category')?.setValue(this.event?.categoryId);
  //   // this.formEvent.get('executionDate')?.setValue(this.event?.endDate);
  //   let dateEvent = this.event?.endDate ? this.datePipe.transform(this.event?.endDate): "";
  //   this.formEvent.get('executionDate')?.setValue(this.parseDate(dateEvent));
  //   this.formEvent.get('winners')?.setValue(this.event?.winnersCount);
  //   this.formEvent.get('description')?.setValue(this.event?.description);
  // }

  // marca el form como si no existieran cambios
  private cleanForm() {
    this.formEvent.markAsPristine();
    this.cdr.detectChanges();
    // console.log("[edicion] => form modificado: ", this.formEvent.dirty);
  }

  private initForm() {
    let dateEvent = this.event?.endDate ? this.datePipe.transform(this.event?.endDate) : "";
    this.setImageTrigger$.next(this.event?.imageUrl);
    // console.log("[img-init] => datos del evento aux: ", this.eventAux);

    this.formEvent = new FormGroup({
      title: new FormControl({ value: this.event?.title, disabled: true }, { validators: [Validators.required] }),
      drawType: new FormControl({ value: this.event?.eventType, disabled: true }, { validators: [Validators.required] }),
      category: new FormControl({ value: this.event?.categoryId, disabled: false }),
      executionDate: new FormControl({ value: this.parseDate(dateEvent), disabled: false }, { validators: [Validators.required] }),
      winners: new FormControl({ value: this.event?.winnersCount, disabled: true }, { validators: [Validators.required] }),
      description: new FormControl({ value: this.event?.description, disabled: false }, { validators: [Validators.required] }),
      image: new FormControl({ value: null, disabled: false }),
      isPrivate: new FormControl({ value: this.event?.isPrivate ?? false, disabled: false }),
      priceRaffle: new FormControl({ value: this.event?.priceOfNumber, disabled: true }),
      quantityNumbersRaffle: new FormControl({ value: this.event?.quantityOfNumbers, disabled: true }),
      // propios de adivinanzas
      targetNumber: new FormControl({ value: "***", disabled: true }),
      maxValue: new FormControl({ value: this.event?.maxValue, disabled: true }),
      minValue: new FormControl({ value: this.event?.minValue, disabled: true }),
      maxAttempts: new FormControl({ value: this.event?.maxAttempts, disabled: false })
    });
    if (this.event?.eventType == EventTypes.GUESSING_CONTEST) {
      this.maxAttempts = this.calculateValue(this.formEvent.get('minValue')?.value, this.formEvent.get('maxValue')?.value);
    }
  }

  private initDataLoadEvent() {
    // Suscribirse a los observables para obtener los datos cuando estén disponibles
    this.configService.categories$.subscribe(categories => {
      this.categories = categories || [];
      this.cdr.markForCheck();
    });

    this.configService.typeEvents$.subscribe(types => {
      this.types = types || [];
      this.cdr.markForCheck();
    });
  }

  private parseDate(fecha: string): string {
    console.log("[edit-event] => fecha recibida: ", fecha);

    const [dia, mes, anio] = fecha.split('/');
    // if(!dia || !mes || !anio){
    //   const [dia, mes, anio] = fecha.split('');
    // }

    const diaFormateado = dia.padStart(2, '0');
    const mesFormateado = mes.padStart(2, '0');

    return `${anio}-${mesFormateado}-${diaFormateado}`;
  }

  public onDiscardChanges() {
    // this.formEvent.reset();
    this.customResetForm();
  }

  public onChangeSelectedImge(image: File | null) {
    // console.log("[imagen] => archivo seleccionado desde el componente de carga: ", image);
    this.formEvent.markAsDirty();
    this.imageEvent = image;
  }

  private customResetForm() {
    // console.log("[img-reset] => datos del evento aux: ", this.eventAux);
    let dateEvent = this.eventAux?.endDate ? this.datePipe.transform(this.eventAux?.endDate) : "";
    this.setImageTrigger$.next(this.eventAux?.imageUrl);

    this.formEvent.reset({
      title: this.eventAux?.title,
      drawType: this.eventAux?.eventType,
      category: this.eventAux?.categoryId,
      executionDate: this.parseDate(dateEvent),
      winners: this.eventAux?.winnersCount,
      description: this.eventAux?.description,
      isPrivate: this.eventAux?.isPrivate ?? false,
      priceRaffle: this.eventAux?.priceOfNumber,
      quantityNumbersRaffle: this.eventAux?.quantityOfNumbers
    });
    this.discardedChanges = false;
  }

  private async getB64Image(image: File) {
    const respConvertImage = await this.parseFileService.convertImageToBase64(image);
    // aca deberiamos ver cuando da error para enviar null
    return respConvertImage;
  }

  private calculateValue(min: number, max: number): number {
    console.log("[calculateValue] => min: ", min);
    console.log("[calculateValue] => max: ", max);
    if (max <= (min - 1)) {
      return 0;
    }
    return Math.round(Math.log2(max - min + 1));
  }

}
