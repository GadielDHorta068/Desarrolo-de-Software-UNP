import { Component, input, Input } from '@angular/core';
import { Events } from '../../../models/events.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard {

  @Input() event!: Events|null;
  customBackground = input<string>('bg-white');

}
