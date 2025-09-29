import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { ImageCroppedEvent, ImageCropperComponent } from 'ngx-image-cropper';
import { ParseFileService } from '../../../services/utils/parseFile.service';

@Component({
  selector: 'app-loader-image',
  imports: [CommonModule, ImageCropperComponent],
  standalone: true,
  templateUrl: './loader-image.html',
  styleUrl: './loader-image.css'
})
export class LoaderImage {

  selectedImage: File | null = null;
  imagePreview: string | null = null;
  currentImagePreview: string | null = null;

  // Image cropper properties
  showImageCropper = false;
  imageChangedEvent: any = '';
  croppedImage: any = '';
  isCropperReady = false;

  errorMessage: string = "";

  @Output() changeSelectedImage = new EventEmitter<File|null>();

  constructor(
    private cdr: ChangeDetectorRef,
    private parseFileService: ParseFileService
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
    // console.log("[imagen] => archivo en memoria: ", this.selectedImage);
    this.imagePreview = null;
    // Resetear el input file
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    this.changeSelectedImage.emit(this.selectedImage);
  }

  removeCurrentImage(): void {
    this.currentImagePreview = null;
    this.selectedImage = null;
    this.imagePreview = null;
    // this.profileForm.patchValue({ imagen: '' });
  }

  // Métodos para el recorte de imagen
    imageCropped(event: ImageCroppedEvent): void {
      // console.log('imageCropped event fired:', event);
      // Usar blob si base64 no está disponible
      if (event.base64) {
        this.croppedImage = event.base64;
      } else if (event.blob) {
        // Convertir blob a base64
        const reader = new FileReader();
        reader.onload = () => {
          this.croppedImage = reader.result as string;
          console.log('croppedImage updated from blob:', this.croppedImage ? 'Image data available' : 'No image data');
          this.cdr.detectChanges();
        };
        reader.readAsDataURL(event.blob);
      }
      console.log('croppedImage updated:', this.croppedImage ? 'Image data available' : 'No image data');
    }

  cancelCrop(): void {
    this.showImageCropper = false;
    this.imageChangedEvent = '';
    this.croppedImage = '';
    this.isCropperReady = false;
    
    // Resetear el input file
    const fileInput = document.getElementById('imagen') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    
    this.cdr.detectChanges();
  }

  imageLoaded(): void {
    // console.log('imageLoaded event fired');
    this.isCropperReady = true;
    // console.log('isCropperReady set to:', this.isCropperReady);
    this.cdr.detectChanges();
  }

  cropperReady(): void {
    console.log('cropperReady event fired');
    // El cropper está listo para usar
  }

  loadImageFailed(): void {
    this.errorMessage = 'Error al cargar la imagen para recortar.';
    this.showImageCropper = false;
    this.cdr.detectChanges();
  }

  confirmCrop(): void {
    // console.log('confirmCrop called - isCropperReady:', this.isCropperReady, 'croppedImage:', this.croppedImage ? 'Available' : 'Not available');
    if (this.croppedImage) {
      // Convertir la imagen recortada a File
      this.parseFileService.dataURLtoFile(this.croppedImage, 'cropped-image.jpg').then(file => {
        this.selectedImage = file;
        // console.log("[imagen] => archivo en memoria: ", this.selectedImage);
        this.imagePreview = this.croppedImage;
        this.showImageCropper = false;
        this.cdr.detectChanges();
        this.changeSelectedImage.emit(this.selectedImage);
      });
    }
  }

}
