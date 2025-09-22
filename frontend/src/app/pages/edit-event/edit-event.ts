import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Events } from '../../models/events.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-event',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-event.html',
  styleUrl: './edit-event.css'
})
export class EditEvent {

  // evento en contexto (debe ser seteado desde donde se quiere interactuar con el dato, por ej el boton de EDITAR)
  event!: Events;

  formEvent: FormGroup;
  
  // tipos de sorteo
  types: {code: string, name: string}[] = [
    { code: 'raffle', name: 'Rifa' },
    { code: 'roulette', name: 'Ruleta' },
  ];
  // categorias de sorteo
  categories: {code: string, name: string}[] = [
    { code: 'sport', name: 'Deporte' },
    { code: 'school', name: 'Escolar' },
    { code: 'caring', name: 'Solidario/ A beneficio' }
  ];

  constructor(){
    // FormGroup({
    //   valueCivilId: new FormControl({ value: civilIdFormatted, disabled: false}, {
    //             validators:[ Validators.required ]

    this.formEvent = new FormGroup({
      title: new FormControl({value: this.event.title, disabled: false}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: this.event.eventType, disabled: false}, {validators:[ Validators.required ]}),
      category: new FormControl({value: this.event.category, disabled: false}),
      executionDate: new FormControl({value: this.event.endDate, disabled: false}, {validators:[ Validators.required ]}),
      winners: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      description: new FormControl({value: this.event.description, disabled: false}, {validators:[ Validators.required ]}),
    });
  }

  public onSaveChanges(){
    console.log("[crearSorteo] => datos del sorteo: ", this.formEvent.value);
  }

}
