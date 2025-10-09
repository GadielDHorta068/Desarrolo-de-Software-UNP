import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Events, EventsCreate, EventsTemp, EventType, EventTypes } from '../../models/events.model';
import { CommonModule } from '@angular/common';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { Category } from '../../services/category.service';
import { configService } from '../../services/config.service';
import { HandleDatePipe } from '../../pipes/handle-date.pipe';
import { LoaderImage } from '../../shared/components/loader-image/loader-image';
import { EventsService } from '../../services/events.service';
import { InfoModal, ModalInfo } from '../../shared/components/modal-info/modal-info';
import { ParseFileService } from '../../services/utils/parseFile.service';

@Component({
  selector: 'app-edit-event',
  imports: [CommonModule, ReactiveFormsModule, LoaderImage, ModalInfo],
  templateUrl: './edit-event.html',
  styleUrl: './edit-event.css',
  providers: [HandleDatePipe]
})
export class EditEvent {
  @ViewChild('modalInfo') modalInfoRef!: ModalInfo;
  dataModal: InfoModal = {title: "Actualización de datos", message: ""};

  // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
  // event!: Events|null;
  event!: EventsTemp|null;
  eventAux!: EventsTemp|null;
  imageEvent: File|null = null;

  formEvent!: FormGroup;
  // tipos de sorteo
  types: EventType[] = [];
  // categorias de sorteo
  categories: Category[] = [];

  constructor(
    private adminEventService: AdminEventService,
    private configService: configService,
    private datePipe: HandleDatePipe,
    private eventService: EventsService,
    private parseFileService: ParseFileService,
    private cdr: ChangeDetectorRef
  ){
    this.configService.initData();
    this.initDataLoadEvent();
    // this.formEvent = new FormGroup({
    //   title: new FormControl({value: this.event?.title, disabled: true}, {validators:[ Validators.required ]}),
    //   drawType: new FormControl({value: this.event?.eventType, disabled: true}, {validators:[ Validators.required ]}),
    //   category: new FormControl({value: this.event?.categoryName, disabled: false}),
    //   executionDate: new FormControl({value: this.event?.endDate, disabled: false}, {validators:[ Validators.required ]}),
    //   winners: new FormControl({value: '', disabled: true}, {validators:[ Validators.required ]}),
    //   description: new FormControl({value: this.event?.description, disabled: false}, {validators:[ Validators.required ]}),
    // });

    this.adminEventService.selectedEvent$.subscribe(
      currentEvent => {
        this.eventAux = currentEvent ? {...currentEvent}: null;
        this.event = currentEvent;
        console.log("[edicion] => evento seleccionado: ", this.event);
        this.initForm();
        // this.updateForm();
      }
    )
  }

  public async onSaveChanges(){
    if(this.imageEvent){
      this.formEvent.get('image')?.setValue(await this.getB64Image(this.imageEvent));
    }
    // console.log("[crearSorteo] => datos del sorteo: ", this.formEvent.value);
    console.log("[crearSorteo] => datos del sorteo completos: ", this.formEvent.getRawValue());
    // TODO: aca falta recuperar la imagen si se subio
    const dataNewEvent = this.getNewEvent(this.formEvent.getRawValue());
    // console.log("[crearSorteo] => datos del sorteo parseado: ", dataNewEvent);
    this.eventService.updateGiveaways(dataNewEvent, ""+this.event?.id, this.event?.creator?.id, ).subscribe({
      next: (response) => {
          // console.log('[initConfig] => nuevo evento creado: ', response);
        this.dataModal.message = "Evento editado correctamente: ", response;
        this.modalInfoRef.open();
        this.cleanForm();
      },
      error: (error) => {
          console.warn('[Eventos]: error al editar el evento: ', error);
          this.dataModal.message = "Error al editar el evento. ", error.error;
          // NOTA: cuando la fecha esta errada no es un json la respuesta, corregir
          this.modalInfoRef.open();
        // });
      }
    });
  }

  private getNewEvent(dataEvent: any){
      return {
        title: dataEvent.title,
        description: dataEvent.description,
        eventType: dataEvent.drawType,
        category: {
          id: Number(dataEvent.category)
        },
        endDate: dataEvent.executionDate,
        winnersCount: dataEvent.winners,
        image: dataEvent.image
      } as EventsCreate;
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
    console.log("[edicion] => form modificado: ", this.formEvent.dirty);
  }

  private initForm(){
    let dateEvent = this.event?.endDate ? this.datePipe.transform(this.event?.endDate): "";

    this.formEvent = new FormGroup({
      title: new FormControl({value: this.event?.title, disabled: true}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: this.event?.eventType, disabled: true}, {validators:[ Validators.required ]}),
      category: new FormControl({value: this.event?.categoryId, disabled: false}),
      executionDate: new FormControl({value: this.parseDate(dateEvent), disabled: false}, {validators:[ Validators.required ]}),
      winners: new FormControl({value: this.event?.winnersCount, disabled: true}, {validators:[ Validators.required ]}),
      description: new FormControl({value: this.event?.description, disabled: false}, {validators:[ Validators.required ]}),
      image: new FormControl({value: null, disabled: false}),
    });
  }

  private initDataLoadEvent(){
    // Suscribirse a los observables para obtener los datos cuando estén disponibles
    this.configService.categories$.subscribe( categories => {
      this.categories = categories || [];
      this.cdr.detectChanges();
    });
    
    this.configService.typeEvents$.subscribe(types => {
      this.types = types || [];
      this.cdr.detectChanges();
    });
  }

  private parseDate(fecha: string): string {
    const [dia, mes, anio] = fecha.split('-');

    const diaFormateado = dia.padStart(2, '0');
    const mesFormateado = mes.padStart(2, '0');

    return `${anio}-${mesFormateado}-${diaFormateado}`;
  }

  public onDiscardChanges(){
    // this.formEvent.reset();
    this.customResetForm();
  }

  public onChangeSelectedImge(image: File|null){
    // console.log("[imagen] => archivo seleccionado desde el componente de carga: ", image);
    this.imageEvent = image;
  }

  private customResetForm(){
    let dateEvent = this.eventAux?.endDate ? this.datePipe.transform(this.eventAux?.endDate): "";
    this.formEvent.reset({
      title: this.eventAux?.title,
      drawType: this.eventAux?.eventType,
      category: this.eventAux?.categoryId,
      executionDate: this.parseDate(dateEvent),
      winners: this.eventAux?.winnersCount,
      description: this.eventAux?.description
    });
  }

  private async getB64Image(image: File){
    const respConvertImage = await this.parseFileService.convertImageToBase64(image);
    // aca deberiamos ver cuando da error para enviar null
    return respConvertImage;
  }

}
