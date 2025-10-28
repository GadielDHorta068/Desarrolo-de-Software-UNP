import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, TwoFactorEnableResponse } from '../../services/auth.service';
import { ImageCroppedEvent, LoadedImage, ImageCropperComponent } from 'ngx-image-cropper';
import { PaymentService } from '../../services/payment.service';
import { Payment, PaymentSortType, PaymentFilter, PaymentStatus } from '../../models/payment.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, ImageCropperComponent],
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
  
  // Image cropper properties
  showImageCropper = false;
  imageChangedEvent: any = '';
  croppedImage: any = '';
  isCropperReady = false;
  
  // 2FA Properties
  is2FAEnabled = false;
  is2FALoading = false;
  qrCodeData: string | null = null;
  recoveryCodes: string[] = [];
  verificationCode = '';
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
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      surname: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      email: [{value: '', disabled: true}],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      cellphone: ['', [this.onlyNumbersValidator, Validators.maxLength(10)]],
      imagen: ['']
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
          cellphone: (user.cellphone ? user.cellphone.replace(/\D/g, '').slice(0, 10) : ''),
          imagen: user.imagen || ''
        });
        // Mostrar imagen actual si existe
        if (user.imagen) {
          this.currentImagePreview = 'data:image/jpeg;base64,' + user.imagen;
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
      
      // Agregar imagen en base64 si existe una nueva seleccionada
      if (this.selectedImage) {
        this.convertImageToBase64(this.selectedImage).then(base64 => {
          profileData.imagen = base64;
          this.submitProfileUpdate(profileData);
        }).catch(error => {
          this.isLoading = false;
          this.errorMessage = 'Error al procesar la imagen.';
          console.error('Image conversion error:', error);
        });
      } else {
        // Si no hay nueva imagen, mantener la actual o enviar vacío si se eliminó
        if (!this.currentImagePreview) {
          profileData.imagen = '';
        }
        this.submitProfileUpdate(profileData);
      }
    } else {
      this.markFormGroupTouched(this.profileForm);
    }
  }

  private submitProfileUpdate(profileData: any): void {
    const imageWasUpdated = this.selectedImage !== null || (!this.currentImagePreview && profileData.imagen === '');
    
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
        
        // Limpiar imagen seleccionada
        this.selectedImage = null;
        this.imagePreview = null;
        
        // Si se actualizó la imagen, forzar actualización del AuthService y recargar página
        if (imageWasUpdated) {
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
    // Aquí podrías agregar una llamada al backend para verificar si 2FA está habilitado
    // Por ahora, asumimos que está deshabilitado por defecto
    this.is2FAEnabled = false;
  }

  enable2FA(): void {
    if (!this.currentUser) {
      this.twoFAErrorMessage = 'Usuario no encontrado';
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
    if (!this.currentUser || !this.verificationCode) {
      this.twoFAErrorMessage = 'Por favor ingresa el código de verificación';
      return;
    }

    if (this.verificationCode.length !== 6) {
      this.twoFAErrorMessage = 'El código debe tener 6 dígitos';
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    this.authService.verify2FA(this.currentUser.nickname, this.verificationCode).subscribe({
      next: (response) => {
        this.is2FALoading = false;
        if (response.verified) {
          this.is2FAEnabled = true;
          this.showRecoveryCodes = true;
          this.twoFASuccessMessage = '¡2FA habilitado exitosamente! Guarda tus códigos de respaldo.';
          this.verificationCode = '';
        } else {
          this.twoFAErrorMessage = 'Código de verificación incorrecto';
        }
        this.cdr.detectChanges();
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

    if (!confirm('¿Estás seguro de que quieres deshabilitar la autenticación de dos factores?')) {
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    this.authService.disable2FA(this.currentUser.nickname).subscribe({
      next: () => {
        this.is2FALoading = false;
        this.is2FAEnabled = false;
        this.qrCodeData = null;
        this.recoveryCodes = [];
        this.showRecoveryCodes = false;
        this.manualSetupKey = '';
        this.twoFASuccessMessage = '2FA deshabilitado exitosamente';
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

    if (!confirm('¿Quieres regenerar tu código QR y códigos de respaldo? Los códigos actuales dejarán de funcionar.')) {
      return;
    }

    this.is2FALoading = true;
    this.twoFAErrorMessage = '';

    this.authService.rotate2FA(this.currentUser.nickname).subscribe({
      next: (response: TwoFactorEnableResponse) => {
        this.is2FALoading = false;
        this.qrCodeData = response.qrCode;
        this.recoveryCodes = response.recoveryCodes;
        this.extractManualSetupKey(response.qrCode);
        this.showRecoveryCodes = true;
        this.twoFASuccessMessage = 'Código QR y códigos de respaldo regenerados exitosamente';
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

  removeCurrentImage(): void {
    this.currentImagePreview = null;
    this.selectedImage = null;
    this.imagePreview = null;
    this.profileForm.patchValue({ imagen: '' });
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

  private formatArgPhone(input: string): string {
    const digits = (input || '').replace(/\D/g, '');
    if (!digits) return '';
    return digits.startsWith('54') ? digits : '54' + digits;
  }
  onCellphoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = (input.value || '').replace(/\D/g, '').slice(0, 10);
    this.profileForm.get('cellphone')?.setValue(digits, { emitEvent: false });
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
            console.log('this.payments set to:', this.payments);
            this.applyPaymentFilter();
            console.log('After applyPaymentFilter, filteredPayments:', this.filteredPayments);
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
    console.log('applyPaymentFilter called');
    console.log('this.payments:', this.payments);
    console.log('this.paymentFilter:', this.paymentFilter);
    this.filteredPayments = this.paymentService.filterPayments(this.payments, this.paymentFilter);
    console.log('this.filteredPayments after filter:', this.filteredPayments);
    console.log('filteredPayments.length:', this.filteredPayments.length);
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

  getStatusText(status: PaymentStatus): string {
    return this.paymentService.getStatusText(status);
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
}