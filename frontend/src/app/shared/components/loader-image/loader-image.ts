import { Component } from '@angular/core';

@Component({
  selector: 'app-loader-image',
  imports: [],
  templateUrl: './loader-image.html',
  styleUrl: './loader-image.css'
})
export class LoaderImage {

  selectedImage: File | null = null;
  imagePreview: string | null = null;
  currentImagePreview: string | null = null;

  // Métodos para manejo de imágenes
  // onImageSelected(event: any): void {
  //   const file = event.target.files[0];
  //   if (file) {
  //     // Validar tipo de archivo
  //     if (!file.type.startsWith('image/')) {
  //       this.errorMessage = 'Por favor selecciona un archivo de imagen válido.';
  //       return;
  //     }
      
  //     // Validar tamaño (máximo 5MB)
  //     if (file.size > 5 * 1024 * 1024) {
  //       this.errorMessage = 'La imagen no puede ser mayor a 5MB.';
  //       return;
  //     }
      
  //     // Limpiar mensajes de error
  //     this.errorMessage = '';
      
  //     // Mostrar el modal de recorte
  //     this.imageChangedEvent = event;
  //     this.showImageCropper = true;
  //     this.isCropperReady = false;
  //     this.cdr.detectChanges();
  //   }
  // }

}
