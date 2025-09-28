import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';

@Component({
  selector: 'app-loader-image',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './loader-image.html',
  styleUrl: './loader-image.css'
})
export class LoaderImage {

  selectedImage: File | null = null;
  imagePreview: string | null = null;
  currentImagePreview: string | null = null;

  showImageCropper = false;
  imageChangedEvent: any = '';
  // croppedImage: any = '';
  isCropperReady = false;

  errorMessage: string = "";

  constructor(
    private cdr: ChangeDetectorRef
  ){}

  // Métodos para manejo de imágenes
  onImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validar tipo de archivo
      if (!file.type.startsWith('image/')) {
        this.errorMessage = 'Por favor selecciona un archivo de imagen válido.';
        console.warn('[loaderImage] => ', this.errorMessage);
        return;
      }
      
      // Validar tamaño (máximo 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'La imagen no puede ser mayor a 5MB.';
        console.warn('[loaderImage] => ', this.errorMessage);
        return;
      }
      
      // Limpiar mensajes de error
      this.errorMessage = '';
      
      // Mostrar el modal de recorte
      this.imageChangedEvent = event;
      this.showImageCropper = true;
      this.isCropperReady = false;
      this.cdr.detectChanges();
    }
  }

  removeSelectedImage(): void {
    this.selectedImage = null;
    this.imagePreview = null;
    // Resetear el input file
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  removeCurrentImage(): void {
    this.currentImagePreview = null;
    this.selectedImage = null;
    this.imagePreview = null;
    // this.profileForm.patchValue({ imagen: '' });
  }

}
