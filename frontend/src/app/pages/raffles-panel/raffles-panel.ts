import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-raffles-panel',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './raffles-panel.html',
  styleUrl: './raffles-panel.css',
  standalone: true
})
export class RafflesPanel {

  formPanel: FormGroup;

  // tipos de sorteo
  types: {code: string, name: string}[] = [
    { code: 'raffle', name: 'Rifa' },
    { code: 'roulette', name: 'Ruleta' },
  ];
  // types = [];

  constructor(){
    // FormGroup({
    //   valueCivilId: new FormControl({ value: civilIdFormatted, disabled: false}, {
    //             validators:[ Validators.required ]

    this.formPanel = new FormGroup({
      title: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
      drawType: new FormControl({value: '', disabled: false}, {validators:[ Validators.required ]}),
    });
  }

  public createDraw(){
    console.log("[crearSorteo] => datos del sorteo: ", this.formPanel.value);
  }

}
