import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { QRCodeComponent, QRCodeErrorCorrectionLevel } from 'angularx-qrcode';

@Component({
  selector: 'app-viewer-qr',
  imports: [CommonModule, QRCodeComponent],
  templateUrl: './viewer-qr.html',
  styleUrl: './viewer-qr.css'
})
export class ViewerQr {
  @Input() data?: any;      // dato a mostrar
  @Input() size?: number;   // tamaño en px
  @Input() errorCorrectLevel?: QRCodeErrorCorrectionLevel;

  readonly DEFAULT_CORRECTION_LEVEL = 'M';
  readonly DEFAULT_SIZE = 100;
  
}
