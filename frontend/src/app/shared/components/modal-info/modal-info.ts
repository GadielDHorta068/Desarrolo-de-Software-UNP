import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';

export interface InfoModal{
  title: string;
  message: string;
}

@Component({
  selector: 'app-modal-info',
  imports: [CommonModule],
  templateUrl: './modal-info.html',
  styleUrl: './modal-info.css'
})
export class ModalInfo {

  @Input() dataModal?: InfoModal;
  show: boolean = false;

  constructor(
    private cdr: ChangeDetectorRef
  ){}

  open() {
    // console.log("[openModalInfo] => evento recibido: ", this.dataModal);
    this.show = true;
    this.cdr.detectChanges();
  }

  close() {
    this.show = false;
    this.cdr.detectChanges();
  }

}
