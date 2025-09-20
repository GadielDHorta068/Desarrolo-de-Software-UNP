import { Component, input, Input } from '@angular/core';
import { Events } from '../../../models/events.model';
import { CommonModule } from '@angular/common';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { ModalDrawInfo } from '../modal-draw-info/modal-draw-info';
import { Router } from '@angular/router';

@Component({
  selector: 'app-draw-card',
  imports: [CommonModule, HandleStatusPipe, HandleIconTypePipe, ModalDrawInfo],
  templateUrl: './draw-card.html',
  styleUrl: './draw-card.css'
})
export class DrawCard {

  @Input() event!: Events|null;
  customBackground = input<string>('bg-white');

  constructor(private router: Router) {}

  public redirectEdit() {
    this.router.navigate(['/event-edit']);
  }
  
}
