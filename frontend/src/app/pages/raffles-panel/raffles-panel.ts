import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { EventsCreate, EventsTemp, EventType, EventTypes, RaffleCreate } from '../../models/events.model';
import { Category } from '../../services/category.service';
import { configService } from '../../services/config.service';
import { EventsService } from '../../services/events.service';
import { AuthService, UserResponse } from '../../services/auth.service';
import { InfoModal } from '../../shared/components/modal-info/modal-info';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { ParseFileService } from '../../services/utils/parseFile.service';
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
  selector: 'app-raffles-panel',
  imports: [CommonModule, ReactiveFormsModule, LoaderImage],
  templateUrl: './raffles-panel.html',
  styleUrl: './raffles-panel.css',
  standalone: true
})
export class RafflesPanel implements OnInit {

  formPanel: FormGroup;
  userCurrent: UserResponse | null = null;

  categories: Category[] = [];
  types: EventType[] = [];
  imageEvent: File | null = null;
  minDate?: string;

  eventTypes = EventTypes;
  // indica si ya se ha creado un evento
  eventCreated: boolean = false;
  maxAttempts: number = 5;

  constructor(
    private configService: configService,
    private eventService: EventsService,
    private authService: AuthService,
    private parseFileService: ParseFileService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.initDateMin();
    this.userCurrent = this.authService.getCurrentUserValue();
    // console.log("[createEvent] => usuario actual: ", this.userCurrent);

    // Inicializar datos de configuración (categorías y tipos de eventos)
    this.configService.initData();

    // Suscribirse a los observables para obtener los datos cuando estén disponibles
    this.configService.categories$.subscribe(categories => {
      this.categories = categories || [];
      this.cdr.detectChanges();
    });

    this.configService.typeEvents$.subscribe(types => {
      this.types = types || [];
      this.cdr.detectChanges();
    });

    // inicializacion del form de creacion de eventos
    this.formPanel = this.initForm();

    this.formPanel.get('drawType')?.valueChanges.subscribe(valor => {
      // console.log('Nuevo tipo de evento:', valor);
      this.updateAvailabilityControls(valor);
    });
  }

  ngOnInit(): void {
    if (!this.userCurrent && this.authService.isAuthenticated()) {
      this.authService.getCurrentUser().subscribe({
        next: user => {
          this.userCurrent = user;
          this.cdr.detectChanges();
        },
        error: () => { }
      });
    }
    this.formPanel.get('minValue')?.valueChanges.subscribe(value => {
      this.maxAttempts = this.calculateValue(value, this.formPanel.get('maxValue')?.value);
      // console.log("maxAttempts: ", this.maxAttempts);
    });
    this.formPanel.get('maxValue')?.valueChanges.subscribe(value => {
      this.maxAttempts = this.calculateValue(this.formPanel.get('minValue')?.value, value);
      // console.log("maxAttempts: ", this.maxAttempts);
    });
  }

  private initDateMin() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow.toISOString().split('T')[0]; // formato yyyy-MM-dd
  }

  public async createDraw() {
    if (!this.formPanel.valid) {
      return;
    }
    const creatorId = this.userCurrent?.id;
    if (!creatorId) {
      this.notificationService.notifyError("Debes iniciar sesión para crear eventos");
      return;
    }
    // aca deberiamos recuperar la imagen seleccionada si la hay
    // this.checkStatusControl();
    if (this.imageEvent) {
      this.formPanel.get('image')?.setValue(await this.getB64Image(this.imageEvent));
    }
    // console.log("[crearSorteo] => datos del sorteo: ", this.formPanel.value);
    const dataNewEvent = this.getNewEvent(this.formPanel.value);

    // console.log("[crearSorteo] => datos del sorteo parseado: ", dataNewEvent);
    this.eventService.createEvent("" + creatorId, dataNewEvent, this.getEventTypeForCreate()).subscribe({
      next: (response) => {
        // console.log('[initConfig] => nuevo evento creado: ', response);
        this.formPanel.disable();
        this.eventCreated = true;
        this.notificationService.notifySuccess("Evento creado correctamente");
        this.router.navigateByUrl("/event/management/" + response.id);
      },
      error: (error) => {
        const message = this.getHttpErrorMessage(error) || "Error al crear el evento";
        this.notificationService.notifyError(message);
      }
    });
  }

  private getEventTypeForCreate() {
    let type = this.formPanel.get('drawType')?.value;
    if (type == EventTypes.GIVEAWAY) {
      return "giveaway";
    }
    if (type == EventTypes.RAFFLES) {
      return "raffle";
    }
    if (type == EventTypes.GUESSING_CONTEST) {
      return "guessing-contest";
    }
    // por default toma el sorteo
    return "raffle";
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

  initForm() {
    return new FormGroup({
      title: new FormControl({ value: '', disabled: false }, { validators: [Validators.required] }),
      drawType: new FormControl({ value: '', disabled: false }, { validators: [Validators.required] }),
      category: new FormControl({ value: '', disabled: false }, { validators: [Validators.required] }),
      executionDate: new FormControl({ value: '', disabled: false }, { validators: [Validators.required] }),
      winners: new FormControl({ value: 1, disabled: false }, { validators: [Validators.required] }),
      description: new FormControl({ value: '', disabled: false }, { validators: [Validators.required] }),
      image: new FormControl({ value: null, disabled: false }),
      isPrivate: new FormControl({ value: false, disabled: false }),
      // propios de rifas
      priceRaffle: new FormControl({ value: '', disabled: false }),
      quantityNumbersRaffle: new FormControl({ value: '', disabled: false }),
      // propios de adivinanzas
      targetNumber: new FormControl({ value: '', disabled: false }),
      maxValue: new FormControl({ value: '', disabled: false }),
      minValue: new FormControl({ value: '', disabled: false }),
      maxAttempts: new FormControl({ value: '', disabled: false })
    });
  }

  public onChangeSelectedImge(image: File | null) {
    // console.log("[imagen] => archivo seleccionado desde el componente de carga: ", image);
    this.imageEvent = image;
  }

  private async getB64Image(image: File | null) {
    if (!image) {
      return;
    }
    const respConvertImage = await this.parseFileService.convertImageToBase64(image);
    // aca deberiamos ver cuando da error para enviar null
    return respConvertImage;
  }

  private getNewEvent(dataEvent: any) {
    let isRaffle = (dataEvent.drawType == EventTypes.RAFFLES);
    let isGuessingContest = (dataEvent.drawType == EventTypes.GUESSING_CONTEST);
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
      isPrivate: !!dataEvent.isPrivate
    }
    if (isRaffle) {
      event.quantityOfNumbers = dataEvent.quantityNumbersRaffle;
      event.priceOfNumber = dataEvent.priceRaffle;
    }
    if (isGuessingContest) {
      event.targetNumber = dataEvent.targetNumber;
      event.maxValue = dataEvent.maxValue;
      event.minValue = dataEvent.minValue;
      event.maxAttempts = dataEvent.maxAttempts;
    }
    // return isRaffle ? event as RaffleCreate: event as EventsCreate;
    return event as EventsCreate;
  }

  // aca actualizamos la visualizacion de nuevos campos en el caso de una rifa por ej
  private updateAvailabilityControls(eventType: string) {
    if (eventType == EventTypes.RAFFLES) {
      this.clearValidatorsGuessingContest();    // limpio controles de adivinanzas
      this.setValidatorsRaffles();
    }
    else {
      if (eventType == EventTypes.GUESSING_CONTEST) {
        this.clearValidatorsRaffles();    // limpio controles de rifa
        this.setValidatorsGuessingContest();
      }
      else {
        // no es ni rifas ni adivinzas, limpio los validadors de esos tipos
        this.clearValidatorsRaffles();
        this.clearValidatorsGuessingContest();
      }
    }
    this.cdr.detectChanges();
  }

  private clearValidatorsRaffles() {
    this.formPanel.get('priceRaffle')?.clearValidators();
    this.formPanel.get('quantityNumbersRaffle')?.clearValidators();
    this.formPanel.get('priceRaffle')?.updateValueAndValidity();
    this.formPanel.get('quantityNumbersRaffle')?.updateValueAndValidity();
  }
  private setValidatorsRaffles() {
    this.formPanel.get('priceRaffle')?.setValidators(Validators.required);
    this.formPanel.get('quantityNumbersRaffle')?.setValidators([Validators.required, Validators.min(50), Validators.max(500)]);
    this.formPanel.get('priceRaffle')?.updateValueAndValidity();
    this.formPanel.get('quantityNumbersRaffle')?.updateValueAndValidity();
  }

  private clearValidatorsGuessingContest() {
    this.formPanel.get('targetNumber')?.clearValidators();
    this.formPanel.get('maxValue')?.clearValidators();
    this.formPanel.get('minValue')?.clearValidators();
    this.formPanel.get('maxAttempts')?.clearValidators();
    this.formPanel.get('targetNumber')?.updateValueAndValidity();
    this.formPanel.get('maxValue')?.updateValueAndValidity();
    this.formPanel.get('minValue')?.updateValueAndValidity();
    this.formPanel.get('maxAttempts')?.updateValueAndValidity();
  }

  private setValidatorsGuessingContest() {
    this.formPanel.get('targetNumber')?.setValidators([
      Validators.required,
      Validators.pattern(/^\d+$/),
      reviewRangeValidators('minValue', 'maxValue') // tu validador cruzado
    ]);
    this.formPanel.get('maxAttempts')?.setValidators([
      Validators.required
    ]);
    this.formPanel.get('minValue')?.setValidators([
      Validators.required,
      Validators.pattern(/^\d+$/),
      lowerRangeValidator('maxValue')
    ]);
    this.formPanel.get('maxValue')?.setValidators([
      Validators.required,
      Validators.pattern(/^\d+$/),
      upperRangeValidator('minValue')
    ]);

    this.formPanel.get('targetNumber')?.updateValueAndValidity();
    this.formPanel.get('maxAttempts')?.updateValueAndValidity();
    this.formPanel.get('minValue')?.updateValueAndValidity();
    this.formPanel.get('maxValue')?.updateValueAndValidity();
  }

  resetForm() {
    this.eventCreated = false;
    this.formPanel.enable();
    this.formPanel.reset({
      winners: 1
    });
    // this.formPanel.reset();
    this.cdr.detectChanges();
  }

  get eventTypeSelected() {
    return this.formPanel.get('drawType')?.value;
  }

  private calculateValue(min: number, max: number): number {
    if (max <= (min - 1)) {
      return 0;
    }
    return Math.round(Math.log2(max - min + 1));
  }

  private getHttpErrorMessage(err: any): string {
    try {
      if (!err) return '';
      if (typeof err.error === 'string') return err.error;
      if (err.error && typeof err.error.message === 'string') return err.error.message;
      if (typeof err.message === 'string') return err.message;
      return '';
    } catch {
      return '';
    }
  }
}
