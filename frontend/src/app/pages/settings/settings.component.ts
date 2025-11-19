import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, TwoFactorEnableResponse } from '../../services/auth.service';
import { ImageCroppedEvent, LoadedImage, ImageCropperComponent } from 'ngx-image-cropper';
import { LoadingIndicator } from '../../shared/components/loading-indicator/loading-indicator';
import { PaymentService } from '../../services/payment.service';
import { Payment, PaymentSortType, PaymentFilter, PaymentStatus } from '../../models/payment.model';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, ImageCropperComponent, LoadingIndicator],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  currentUser: any = null;
  isLoading = false;
  isPasswordLoading = false;
  successMessage = '';
  errorMessage = '';
  passwordSuccessMessage = '';
  passwordErrorMessage = '';
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;
  activeTab: string = 'profile';
  selectedImage: File | null = null;
  imagePreview: string | null = null;
  currentImagePreview: string | null = null;
  // Cover image properties
  selectedCoverImage: File | null = null;
  coverImagePreview: string | null = null;
  currentCoverImagePreview: string | null = null;
  
  // Image cropper properties
  showImageCropper = false;
  imageChangedEvent: any = '';
  croppedImage: any = '';
  isCropperReady = false;
  // Cover cropper properties
  showCoverCropper = false;
  coverImageChangedEvent: any = '';
  croppedCoverImage: any = '';
  isCoverCropperReady = false;
  
  // 2FA Properties
  is2FAEnabled = false;
  is2FALoading = false;
  qrCodeData: string | null = null;
  recoveryCodes: string[] = [];
  verificationCode = '';
  recoveryCodeInput = '';
  twoFAForm!: FormGroup;
  twoFASuccessMessage = '';
  twoFAErrorMessage = '';
  showRecoveryCodes = false;
  manualSetupKey = '';

  // Payment History Properties
  payments: Payment[] = [];
  filteredPayments: Payment[] = [];
  isPaymentsLoading = false;
  paymentErrorMessage = '';
  paymentErrorType: 'network' | 'auth' | 'notfound' | 'server' | null = null;
  paymentFilter: PaymentFilter = {
    sortBy: PaymentSortType.DATE,
    sortOrder: 'desc'
  };
  expandedPaymentId: number | null = null;
  paymentSortTypes = PaymentSortType;
  paymentStatuses = PaymentStatus;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    public router: Router,
    private cdr: ChangeDetectorRef,
    private paymentService: PaymentService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    // Inicializar datos del usuario si están disponibles
    this.authService.initializeUserData();
    
    this.loadCurrentUser();
    this.check2FAStatus();
    this.twoFAForm = this.fb.group({
      totp: ['', [Validators.minLength(6), Validators.maxLength(6), Validators.pattern(/^\d{6}$/)]],
      recoveryCode: ['']
    });
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      surname: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      email: [{value: '', disabled: true}],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      cellphone: ['', [this.onlyNumbersValidator, Validators.maxLength(10)]],
      description: ['', [Validators.maxLength(500)]],
      imagen: [''],
      coverImage: [''],
      twitter: ['', [Validators.maxLength(255)]],
      facebook: ['', [Validators.maxLength(255)]],
      instagram: ['', [Validators.maxLength(255)]],
      linkedin: ['', [Validators.maxLength(255)]],
      website: ['', [Validators.maxLength(255)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  // Validador personalizado para solo letras
  private onlyLettersValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // No validar si está vacío (required se encarga de eso)
    }
    const lettersOnlyRegex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]+$/;
    const valid = lettersOnlyRegex.test(control.value);
    return valid ? null : { onlyLetters: true };
  }

  // Validador personalizado para solo números
  private onlyNumbersValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // No validar si está vacío (campo opcional)
    }
    const numbersOnlyRegex = /^[0-9]*$/;
    const valid = numbersOnlyRegex.test(control.value);
    return valid ? null : { onlyNumbers: true };
  }

  private loadCurrentUser(): void {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.profileForm.patchValue({
          name: user.name,
          surname: user.surname,
          email: user.email,
          nickname: user.nickname,
          cellphone: (user.cellphone ? this.normalizePhoneNumber(user.cellphone) : ''),
          description: user.description || '',
          imagen: user.imagen || '',
          coverImage: user.coverImage || '',
          twitter: user.twitter || '',
          facebook: user.facebook || '',
          instagram: user.instagram || '',
          linkedin: user.linkedin || '',
          website: user.website || ''
        });
        // Mostrar imagen actual si existe
        if (user.imagen) {
          this.currentImagePreview = 'data:image/jpeg;base64,' + user.imagen;
        }
        // Mostrar portada actual si existe
        if (user.coverImage) {
          // Acepta tanto base64 puro como data URL
          this.currentCoverImagePreview = user.coverImage.startsWith('data:')
            ? user.coverImage
            : 'data:image/jpeg;base64,' + user.coverImage;
        }
        // Forzar detección de cambios después de cargar datos
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading user:', error);
        this.errorMessage = 'Error al cargar los datos del usuario';
        // Forzar detección de cambios en caso de error
        this.cdr.detectChanges();
      }
    });
  }

  onProfileSubmit(): void {
    if (this.profileForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const profileData = this.profileForm.value;
      
      // Incluir el email del usuario actual ya que el campo está deshabilitado
      if (this.currentUser && this.currentUser.email) {
        profileData.email = this.currentUser.email;
      }
      
      // Formatear teléfono a número argentino guardado como 54 + dígitos
      if (profileData.cellphone) {
        profileData.cellphone = this.formatArgPhone(profileData.cellphone);
      }
      // Resolver imágenes a enviar (perfil y portada)
      const conversions: Promise<void>[] = [];
      if (this.selectedImage) {
        conversions.push(
          this.convertImageToBase64(this.selectedImage).then(base64 => { profileData.imagen = base64; })
        );
      } else if (!this.currentImagePreview) {
        profileData.imagen = '';
      }

      if (this.selectedCoverImage) {
        conversions.push(
          this.convertImageToBase64(this.selectedCoverImage).then(base64 => { profileData.coverImage = base64; })
        );
      } else if (!this.currentCoverImagePreview) {
        profileData.coverImage = '';
      }

      Promise.all(conversions).then(() => {
        this.submitProfileUpdate(profileData);
      }).catch(error => {
        this.isLoading = false;
        this.errorMessage = 'Error al procesar imágenes.';
        console.error('Image conversion error:', error);
      });
    } else {
      this.markFormGroupTouched(this.profileForm);
    }
  }

  private submitProfileUpdate(profileData: any): void {
    const imageWasUpdated = this.selectedImage !== null || (!this.currentImagePreview && profileData.imagen === '');
    const coverWasUpdated = this.selectedCoverImage !== null || (!this.currentCoverImagePreview && profileData.coverImage === '');
    
    this.authService.updateProfile(profileData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Perfil actualizado correctamente';
        // Forzar detección de cambios
        this.cdr.detectChanges();
        
        // Actualizar los datos del usuario en el servicio
        this.currentUser = { ...this.currentUser, ...response };
        
        // Actualizar preview de imagen
        if (response.imagen) {
          this.currentImagePreview = 'data:image/jpeg;base64,' + response.imagen;
        } else {
          this.currentImagePreview = null;
        }
        // Actualizar preview de portada
        if (response.coverImage) {
          this.currentCoverImagePreview = 'data:image/jpeg;base64,' + response.coverImage;
        } else {
          this.currentCoverImagePreview = null;
        }
        
        // Limpiar imagen seleccionada
        this.selectedImage = null;
        this.imagePreview = null;
        // Limpiar portada seleccionada
        this.selectedCoverImage = null;
        this.coverImagePreview = null;
        
        // Si se actualizó la imagen, forzar actualización del AuthService y recargar página
        if (imageWasUpdated || coverWasUpdated) {
          this.authService.initializeUserData();
          this.successMessage = 'Perfil actualizado correctamente. Refrescando página...';
          this.cdr.detectChanges();
          
          // Recargar página después de 2 segundos para actualizar caché de imágenes
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        } else {
          // Limpiar mensaje después de 3 segundos si no se actualizó imagen
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al actualizar perfil:', error);
        this.errorMessage = error.error || 'Error al actualizar el perfil';
        // Forzar detección de cambios
        this.cdr.detectChanges();
      }
    });
  }

  onPasswordSubmit(): void {
    if (this.passwordForm.valid) {
      this.isPasswordLoading = true;
      this.passwordErrorMessage = '';
      this.passwordSuccessMessage = '';

      const passwordData = {
        currentPassword: this.passwordForm.value.currentPassword,
        newPassword: this.passwordForm.value.newPassword
      };
      
      this.authService.changePassword(passwordData).subscribe({
        next: (response) => {
          this.isPasswordLoading = false;
          this.passwordSuccessMessage = 'Contraseña actualizada correctamente';
          this.passwordForm.reset();
          // Forzar detección de cambios
          this.cdr.detectChanges();
          
          // Limpiar mensaje después de 3 segundos
          setTimeout(() => {
            this.passwordSuccessMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.isPasswordLoading = false;
          console.error('Error al cambiar contraseña:', error);
          this.passwordErrorMessage = error.error || 'Error al cambiar la contraseña';
          // Forzar detección de cambios
          this.cdr.detectChanges();
        }
      });
    } else {
      this.markFormGroupTouched(this.passwordForm);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(formGroup: FormGroup, fieldName: string): string | null {
    const field = formGroup.get(fieldName);
    if (field && field.errors && field.touched) {
      if (field.errors['required']) {
        return 'Este campo es requerido';
      }
      if (field.errors['email']) {
        return 'Email inválido';
      }
      if (field.errors['minlength']) {
        return `Mínimo ${field.errors['minlength'].requiredLength} caracteres`;
      }
      if (field.errors['maxlength']) {
        return 'Máximo 10 dígitos';
      }
      if (field.errors['passwordMismatch']) {
        return 'Las contraseñas no coinciden';
      }
      if (field.errors['onlyLetters']) {
        return 'Solo se permiten letras';
      }
      if (field.errors['onlyNumbers']) {
        return 'Solo números';
      }
    }
    return null;
  }

  toggleCurrentPasswordVisibility(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.passwordSuccessMessage = '';
    this.passwordErrorMessage = '';
    // Forzar detección de cambios al limpiar mensajes
    this.cdr.detectChanges();
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  deleteAccount(): void {
    if (confirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) {
      // Implementar lógica de eliminación de cuenta
      console.log('Eliminar cuenta');
    }
  }

  // 2FA Methods
  private check2FAStatus(): void {
    if (!this.currentUser) {
      this.authService.getCurrentUser().subscribe({
        next: (user) => {
          this.currentUser = user;
          this.fetch2FAStatus(user.nickname);
        },
        error: () => {
          this.is2FAEnabled = false;
        }
      });
      return;
    }
    this.fetch2FAStatus(this.currentUser.nickname);
  }

  private fetch2FAStatus(nickname: string): void {
    this.authService.get2FAStatus(nickname).subscribe({
      next: (status) => {
        this.is2FAEnabled = !!status.twoFactorEnabled;
        this.cdr.detectChanges();
      },
      error: () => {
        this.is2FAEnabled = false;
        this.cdr.detectChanges();
      }
    });
  }

  enable2FA(): void {
    if (!this.currentUser) {
      this.twoFAErrorMessage = 'Usuario no encontrado';
      return;
    }

    if (this.is2FAEnabled) {
      this.twoFAErrorMessage = '2FA ya está habilitado. Usa "Regenerar Código QR" o "Deshabilitar 2FA".';
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';
    this.twoFASuccessMessage = '';

    this.authService.enable2FA(this.currentUser.nickname).subscribe({
      next: (response: TwoFactorEnableResponse) => {
        this.is2FALoading = false;
        this.qrCodeData = response.qrCode;
        this.recoveryCodes = response.recoveryCodes;
        this.extractManualSetupKey(response.qrCode);
        this.twoFASuccessMessage = 'Código QR generado. Escanéalo con tu aplicación de autenticación.';
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.is2FALoading = false;
        console.error('Error enabling 2FA:', error);
        this.twoFAErrorMessage = error.error?.message || 'Error al habilitar 2FA';
        this.cdr.detectChanges();
      }
    });
  }

  verify2FA(): void {
    const totp = this.twoFAForm.get('totp')?.value || '';
    if (!this.currentUser || !totp) {
      this.twoFAErrorMessage = 'Por favor ingresa el código de verificación';
      return;
    }

    if (totp.length !== 6) {
      this.twoFAErrorMessage = 'El código debe tener 6 dígitos';
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    this.authService.verify2FA(this.currentUser.nickname, totp).subscribe({
      next: (response) => {
        this.is2FALoading = false;
        if (response.verified) {
          this.is2FAEnabled = true;
          this.showRecoveryCodes = true;
          this.cdr.detectChanges();
          this.twoFASuccessMessage = '¡2FA habilitado exitosamente! Guarda tus códigos de respaldo.';
          this.twoFAForm.reset({ totp: '', recoveryCode: '' });
        } else {
          this.twoFAErrorMessage = 'Código de verificación incorrecto';
        }
      },
      error: (error) => {
        this.is2FALoading = false;
        console.error('Error verifying 2FA:', error);
        this.twoFAErrorMessage = error.error?.message || 'Error al verificar el código';
        this.cdr.detectChanges();
      }
    });
  }

  disable2FA(): void {
    if (!this.currentUser) {
      this.twoFAErrorMessage = 'Usuario no encontrado';
      return;
    }

    if (!confirm('Para deshabilitar 2FA se requiere un código de verificación (TOTP) o un código de respaldo. ¿Deseas continuar?')) {
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    const totp = this.twoFAForm.get('totp')?.value || '';
    const recovery = this.twoFAForm.get('recoveryCode')?.value || '';
    const payload: any = {};
    if (totp && totp.length === 6) {
      payload.code = totp;
    } else if (recovery) {
      payload.recoveryCode = recovery.trim();
    } else {
      this.is2FALoading = false;
      this.twoFAErrorMessage = 'Ingresa el TOTP de 6 dígitos o un código de respaldo';
      this.cdr.detectChanges();
      return;
    }

    this.authService.disable2FA(this.currentUser.nickname, payload).subscribe({
      next: () => {
        this.is2FALoading = false;
        this.is2FAEnabled = false;
        this.qrCodeData = null;
        this.recoveryCodes = [];
        this.showRecoveryCodes = false;
        this.manualSetupKey = '';
        this.twoFASuccessMessage = '2FA deshabilitado exitosamente';
        this.twoFAForm.reset({ totp: '', recoveryCode: '' });
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.is2FALoading = false;
        console.error('Error disabling 2FA:', error);
        this.twoFAErrorMessage = error.error?.message || 'Error al deshabilitar 2FA';
        this.cdr.detectChanges();
      }
    });
  }

  rotate2FA(): void {
    if (!this.currentUser) {
      this.twoFAErrorMessage = 'Usuario no encontrado';
      return;
    }
    if (!confirm('Para regenerar el QR se requiere verificación (TOTP) o un código de respaldo. ¿Deseas continuar?')) {
      return;
    }

    const totp = this.twoFAForm.get('totp')?.value || '';
    const recovery = this.twoFAForm.get('recoveryCode')?.value || '';
    const payload: any = {};
    if (totp && totp.length === 6) {
      payload.code = totp;
    } else if (recovery) {
      payload.recoveryCode = recovery.trim();
    } else {
      this.twoFAErrorMessage = 'Ingresa el TOTP de 6 dígitos o un código de respaldo';
      this.cdr.detectChanges();
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    this.authService.rotate2FA(this.currentUser.nickname, payload).subscribe({
      next: (response: TwoFactorEnableResponse) => {
        this.is2FALoading = false;
        this.qrCodeData = response.qrCode;
        this.recoveryCodes = response.recoveryCodes;
        this.extractManualSetupKey(response.qrCode);
        this.showRecoveryCodes = true;
        this.twoFASuccessMessage = 'Código QR y códigos de respaldo regenerados exitosamente';
        this.twoFAForm.reset({ totp: '', recoveryCode: '' });
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.is2FALoading = false;
        console.error('Error rotating 2FA:', error);
        this.twoFAErrorMessage = error.error?.message || 'Error al regenerar 2FA';
        this.cdr.detectChanges();
      }
    });
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.twoFASuccessMessage = 'Copiado al portapapeles';
      setTimeout(() => {
        this.twoFASuccessMessage = '';
        this.cdr.detectChanges();
      }, 2000);
    }).catch(() => {
      this.twoFAErrorMessage = 'Error al copiar al portapapeles';
    });
  }

  downloadRecoveryCodes(): void {
    const content = this.recoveryCodes.join('\n');
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '2fa-recovery-codes.txt';
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private extractManualSetupKey(qrCode: string): void {
    // Extraer la clave del código QR para configuración manual
    try {
      const url = new URL(qrCode.replace('data:image/png;base64,', '').replace(/.*otpauth:\/\/totp\//, 'otpauth://totp/'));
      this.manualSetupKey = url.searchParams.get('secret') || 'No disponible';
    } catch {
      this.manualSetupKey = 'No disponible';
    }
  }

  private clear2FAMessages(): void {
    this.twoFASuccessMessage = '';
    this.twoFAErrorMessage = '';
  }

  private clearPaymentMessages(): void {
    this.paymentErrorMessage = '';
    this.paymentErrorType = null;
  }

  setActiveTab(tab: string): void {
    console.log('setActiveTab called with:', tab);
    this.activeTab = tab;
    this.clearMessages();
    this.clear2FAMessages();
    this.clearPaymentMessages();
    
    // Cargar historial de pagos cuando se selecciona la pestaña de transacciones
    if (tab === 'transacciones') {
      console.log('Transacciones tab selected, current user:', this.currentUser);
      if (!this.currentUser) {
        console.log('No current user, loading user first');
        this.loadCurrentUser();
        // Esperar un poco y luego intentar cargar los pagos
        setTimeout(() => {
          console.log('After loading user, current user:', this.currentUser);
          this.loadPaymentHistory();
        }, 500);
      } else {
        this.loadPaymentHistory();
      }
    }
  }

  isActiveTab(tab: string): boolean {
    return this.activeTab === tab;
  }

  // Métodos para manejo de imágenes
  onImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validar tipo de archivo
      if (!file.type.startsWith('image/')) {
        this.errorMessage = 'Por favor selecciona un archivo de imagen válido.';
        return;
      }
      
      // Validar tamaño (máximo 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'La imagen no puede ser mayor a 5MB.';
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

  // Métodos para manejo de imagen de portada
  onCoverSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.errorMessage = 'Por favor selecciona un archivo de imagen válido.';
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'La imagen no puede ser mayor a 5MB.';
        return;
      }
      this.errorMessage = '';
      this.coverImageChangedEvent = event;
      this.showCoverCropper = true;
      this.isCoverCropperReady = false;
      this.cdr.detectChanges();
    }
  }

  removeCurrentImage(): void {
    this.currentImagePreview = null;
    this.selectedImage = null;
    this.imagePreview = null;
    this.profileForm.patchValue({ imagen: '' });
  }

  removeCurrentCoverImage(): void {
    this.currentCoverImagePreview = null;
    this.selectedCoverImage = null;
    this.coverImagePreview = null;
    this.profileForm.patchValue({ coverImage: '' });
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

  removeSelectedCoverImage(): void {
    this.selectedCoverImage = null;
    this.coverImagePreview = null;
    const fileInput = document.getElementById('coverImage') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  private convertImageToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result as string;
        // Remover el prefijo data:image/...;base64,
        const base64Data = base64.split(',')[1];
        resolve(base64Data);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  // Métodos para el recorte de imagen
  imageCropped(event: ImageCroppedEvent): void {
    console.log('imageCropped event fired:', event);
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

  imageLoaded(): void {
    console.log('imageLoaded event fired');
    this.isCropperReady = true;
    console.log('isCropperReady set to:', this.isCropperReady);
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
    console.log('confirmCrop called - isCropperReady:', this.isCropperReady, 'croppedImage:', this.croppedImage ? 'Available' : 'Not available');
    if (this.croppedImage) {
      // Convertir la imagen recortada a File
      this.dataURLtoFile(this.croppedImage, 'cropped-image.jpg').then(file => {
        this.selectedImage = file;
        this.imagePreview = this.croppedImage;
        this.showImageCropper = false;
        this.cdr.detectChanges();
      });
    }
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

  // Métodos para el recorte de portada
  coverImageCropped(event: ImageCroppedEvent): void {
    if (event.base64) {
      this.croppedCoverImage = event.base64;
    } else if (event.blob) {
      const reader = new FileReader();
      reader.onload = () => {
        this.croppedCoverImage = reader.result as string;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(event.blob);
    }
  }

  coverImageLoaded(): void {
    this.isCoverCropperReady = true;
    this.cdr.detectChanges();
  }

  coverCropperReady(): void {}

  coverLoadImageFailed(): void {
    this.errorMessage = 'Error al cargar la imagen de portada para recortar.';
    this.showCoverCropper = false;
    this.cdr.detectChanges();
  }

  confirmCoverCrop(): void {
    if (this.croppedCoverImage) {
      this.dataURLtoFile(this.croppedCoverImage, 'cropped-cover.jpg').then(file => {
        this.selectedCoverImage = file;
        this.coverImagePreview = this.croppedCoverImage;
        this.showCoverCropper = false;
        this.cdr.detectChanges();
      });
    }
  }

  cancelCoverCrop(): void {
    this.showCoverCropper = false;
    this.coverImageChangedEvent = '';
    this.croppedCoverImage = '';
    this.isCoverCropperReady = false;
    const fileInput = document.getElementById('coverImage') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    this.cdr.detectChanges();
  }

  private dataURLtoFile(dataurl: string, filename: string): Promise<File> {
    return new Promise((resolve) => {
      const arr = dataurl.split(',');
      const mime = arr[0].match(/:(.*?);/)![1];
      const bstr = atob(arr[1]);
      let n = bstr.length;
      const u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      resolve(new File([u8arr], filename, { type: mime }));
    });
  }

  // Método privado para normalizar números de teléfono
  private normalizePhoneNumber(phoneNumber: string): string {
    const digits = (phoneNumber || '').replace(/\D/g, ''); // solo dígitos
    
    // Si el número tiene 12 dígitos y empieza con "54", recortar los primeros 2 dígitos
    if (digits.length === 12 && digits.startsWith('54')) {
      return digits.substring(2); // Remover los primeros 2 dígitos (54)
    }
    
    // Si tiene más de 10 dígitos, tomar solo los primeros 10
    return digits.slice(0, 10);
  }

  private formatArgPhone(input: string): string {
    const digits = (input || '').replace(/\D/g, '');
    if (!digits) return '';
    return digits.startsWith('54') ? digits : '54' + digits;
  }
  onCellphoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const normalizedDigits = this.normalizePhoneNumber(input.value || '');
    this.profileForm.get('cellphone')?.setValue(normalizedDigits, { emitEvent: false });
  }

  // Payment History Methods
  loadPaymentHistory(): void {
    console.log('loadPaymentHistory called');
    console.log('currentUser:', this.currentUser);
    
    // Limpiar errores previos
    this.clearPaymentMessages();
    
    if (!this.currentUser?.id) {
      console.log('No current user or user ID, loading current user first');
      this.loadCurrentUser();
      // Intentar cargar pagos después de un breve delay para permitir que se cargue el usuario
      setTimeout(() => {
        if (this.currentUser?.id) {
          this.loadPaymentHistory();
        } else {
          console.error('Unable to load current user for payment history');
          this.paymentErrorMessage = 'No se pudo cargar la información del usuario.';
          this.paymentErrorType = 'auth';
        }
      }, 1000);
      return;
    }
    
    // El AuthInterceptor maneja automáticamente la autenticación
    
    console.log('Loading payment history for user ID:', this.currentUser.id);
    this.isPaymentsLoading = true;
    
    // Cargar pagos donde el usuario es el pagador
    this.paymentService.getPaymentsByUser(this.currentUser.id).subscribe({
      next: (userPayments) => {
        console.log('User payments loaded:', userPayments);
        // Cargar pagos donde el usuario es el receptor
        this.paymentService.getPaymentsByReceiver(this.currentUser.id).subscribe({
          next: (receiverPayments) => {
            console.log('Receiver payments loaded:', receiverPayments);
            // Combinar ambos arrays y eliminar duplicados
            const allPayments = [...userPayments, ...receiverPayments];
            const uniquePayments = allPayments.filter((payment, index, self) => 
              index === self.findIndex(p => p.id === payment.id)
            );
            
            console.log('Total unique payments:', uniquePayments.length);
            this.payments = uniquePayments;
            this.applyPaymentFilter();
            this.isPaymentsLoading = false;
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Error loading receiver payments:', error);
            this.handlePaymentError(error);
            // Si falla cargar los pagos del receptor, al menos mostrar los del usuario
            this.payments = userPayments;
            this.applyPaymentFilter();
            this.isPaymentsLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (error) => {
        console.error('Error loading user payments:', error);
        this.handlePaymentError(error);
        this.isPaymentsLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private handlePaymentError(error: any): void {
    console.error('Payment error details:', error);
    
    if (error.status === 403 || error.status === 401) {
      this.paymentErrorMessage = 'Tu sesión ha expirado. Por favor, inicia sesión nuevamente.';
      this.paymentErrorType = 'auth';
      console.error('Authentication error - token may be expired');
    } else if (error.status === 404) {
      this.paymentErrorMessage = 'No se encontraron transacciones para tu cuenta.';
      this.paymentErrorType = 'notfound';
      console.log('No payments found for user');
    } else if (error.status === 0) {
      this.paymentErrorMessage = 'No se pudo conectar con el servidor. Verifica tu conexión a internet.';
      this.paymentErrorType = 'network';
      console.error('Network error - server may be down');
    } else if (error.status >= 500) {
      this.paymentErrorMessage = 'Error interno del servidor. Intenta nuevamente en unos minutos.';
      this.paymentErrorType = 'server';
      console.error('Server error:', error);
    } else {
      this.paymentErrorMessage = error.error?.message || error.message || 'Error desconocido al cargar las transacciones.';
      this.paymentErrorType = 'server';
      console.error('Unknown error loading payments:', error);
    }
  }

  applyPaymentFilter(): void {
    this.filteredPayments = this.paymentService.filterPayments(this.payments, this.paymentFilter);
  }

  onSortChange(sortType: PaymentSortType): void {
    this.paymentFilter.sortBy = sortType;
    this.applyPaymentFilter();
  }

  togglePaymentExpansion(paymentId: number): void {
    this.expandedPaymentId = this.expandedPaymentId === paymentId ? null : paymentId;
  }

  isPaymentExpanded(paymentId: number): boolean {
    return this.expandedPaymentId === paymentId;
  }

  getStatusColor(status: PaymentStatus): string {
    return this.paymentService.getStatusColor(status);
  }

 

  formatDate(date: string): string {
    return this.paymentService.formatDate(date);
  }

  getSortTypeText(sortType: PaymentSortType): string {
    switch (sortType) {
      case PaymentSortType.DATE:
        return 'Fecha de llegada';
      case PaymentSortType.SENDER:
        return 'Orden alfabético del remitente';
      case PaymentSortType.EVENT:
        return 'Orden alfabético del evento';
      default:
        return 'Fecha de llegada';
    }
  }

  isUserSender(payment: Payment): boolean {
    return payment.user.id === this.currentUser?.id;
  }

  isUserReceiver(payment: Payment): boolean {
    return payment.receiver.id === this.currentUser?.id;
  }

  getApprovedPaymentsCount(): number {
    return this.filteredPayments.filter(payment => payment.status === PaymentStatus.APPROVED).length;
  }

  getPendingPaymentsCount(): number {
    return this.filteredPayments.filter(payment => payment.status === PaymentStatus.PENDING).length;
  }

  retryLoadPayments(): void {
    this.loadPaymentHistory();
  }

  formatPaymentDate(dateArray: number[]): string {
    if (!dateArray || dateArray.length < 3) {
      return 'Fecha no disponible';
    }
    
    // El backend devuelve [año, mes, día, hora, minuto, segundo, nanosegundo]
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
    const date = new Date(year, month - 1, day, hour, minute, second);
    
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  }

  exportToPDF(): void {
    const doc = new jsPDF();
    
    // Función auxiliar para obtener el nombre del usuario
    const getUserDisplayName = (user: any): string => {
      if (!user) return 'N/A';
      
      if (user.username) return user.username;
      if (user.name && user.surname) return `${user.name} ${user.surname}`;
      if (user.name) return user.name;
      if (user.nickname) return user.nickname;
      return 'N/A';
    };
    
    // Título del documento
    doc.setFontSize(18);
    doc.text('Historial de Transacciones', 14, 22);
    
    // Información del usuario
    doc.setFontSize(12);
    doc.text(`Usuario: ${getUserDisplayName(this.currentUser)}`, 14, 35);
    doc.text(`Fecha de exportación: ${new Date().toLocaleDateString('es-ES')}`, 14, 45);
    
    // Preparar datos para la tabla
    const tableData = this.filteredPayments.map(payment => [
      this.formatPaymentDate(payment.createdAt),
      payment.description || payment.event?.description || 'Sin descripción',
      getUserDisplayName(payment.user),
      getUserDisplayName(payment.receiver),
      this.formatCurrency(payment.amount, payment.currency),
      this.getStatusText(payment.status),
      payment.event?.title || 'Sin evento'
    ]);
    
    // Crear tabla
    autoTable(doc, {
      head: [['Fecha', 'Descripción', 'Pagador', 'Receptor', 'Monto', 'Estado', 'Evento']],
      body: tableData,
      startY: 55,
      styles: {
        fontSize: 8,
        cellPadding: 2
      },
      headStyles: {
        fillColor: [41, 128, 185],
        textColor: 255
      },
      columnStyles: {
        0: { cellWidth: 25 },
        1: { cellWidth: 35 },
        2: { cellWidth: 25 },
        3: { cellWidth: 25 },
        4: { cellWidth: 20 },
        5: { cellWidth: 20 },
        6: { cellWidth: 30 }
      }
    });
    
    // Guardar el archivo
    doc.save(`transacciones_${new Date().toISOString().split('T')[0]}.pdf`);
  }

  exportToExcel(): void {
    // Función auxiliar para obtener el nombre del usuario
    const getUserDisplayName = (user: any): string => {
      if (!user) return 'N/A';
      
      if (user.username) return user.username;
      if (user.name && user.surname) return `${user.name} ${user.surname}`;
      if (user.name) return user.name;
      if (user.nickname) return user.nickname;
      return 'N/A';
    };

    // Preparar datos para Excel con mejor formato
    const excelData = this.filteredPayments.map(payment => ({
      'Fecha': this.formatPaymentDate(payment.createdAt),
      'Descripción': payment.description || payment.event?.description || 'Sin descripción',
      'Pagador': getUserDisplayName(payment.user),
      'Receptor': getUserDisplayName(payment.receiver),
      'Monto': `${payment.amount} ${payment.currency}`,
      'Estado': this.getStatusText(payment.status),
      'Evento': payment.event?.title || 'Sin evento',
      'ID Transacción': payment.id
    }));
    
    // Crear libro de trabajo
    const workbook = XLSX.utils.book_new();
    
    // Agregar información del encabezado
    const headerInfo = [
      ['Historial de Transacciones'],
      [`Usuario: ${getUserDisplayName(this.currentUser)}`],
      [`Fecha de exportación: ${new Date().toLocaleDateString('es-ES')}`],
      [`Total de transacciones: ${this.filteredPayments.length}`],
      []
    ];
    
    // Crear hoja de trabajo con encabezado
    const worksheet = XLSX.utils.aoa_to_sheet(headerInfo);
    
    // Agregar los datos de la tabla
    XLSX.utils.sheet_add_json(worksheet, excelData, { 
      origin: 'A6', // Comenzar después del encabezado
      skipHeader: false 
    });
    
    // Configurar el ancho de las columnas para evitar que se pierda texto
    const columnWidths = [
      { wch: 15 }, // Fecha
      { wch: 25 }, // Descripción
      { wch: 20 }, // Pagador
      { wch: 20 }, // Receptor
      { wch: 15 }, // Monto
      { wch: 12 }, // Estado
      { wch: 25 }, // Evento
      { wch: 12 }  // ID Transacción
    ];
    worksheet['!cols'] = columnWidths;
    
    // Agregar hoja al libro
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Transacciones');
    
    // Generar archivo y descargar
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, `transacciones_${new Date().toISOString().split('T')[0]}.xlsx`);
  }

  getStatusText(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.PENDING:
        return 'Pendiente';
      case PaymentStatus.APPROVED:
        return 'Aprobado';
      case PaymentStatus.REJECTED:
        return 'Rechazado';
      case PaymentStatus.CANCELLED:
        return 'Cancelado';
      case PaymentStatus.REFUNDED:
        return 'Reembolsado';
      default:
        return 'Desconocido';
    }
  }
}
