import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, TwoFactorEnableResponse } from '../../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadCurrentUser();
    this.check2FAStatus();
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      surname: ['', [Validators.required, Validators.minLength(2), this.onlyLettersValidator]],
      email: ['', [Validators.required, Validators.email]],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      cellphone: ['', [this.onlyNumbersValidator]],
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
    const numbersOnlyRegex = /^[0-9+\s-]+$/;
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
          cellphone: user.cellphone || '',
          imagen: user.imagen || ''
        });
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
      
      this.authService.updateProfile(profileData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Perfil actualizado correctamente';
          // Forzar detección de cambios
          this.cdr.detectChanges();
          
          // Actualizar los datos del usuario en el servicio
          this.loadCurrentUser();
          
          // Limpiar mensaje después de 3 segundos
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Error al actualizar perfil:', error);
          this.errorMessage = error.error || 'Error al actualizar el perfil';
          // Forzar detección de cambios
          this.cdr.detectChanges();
        }
      });
    } else {
      this.markFormGroupTouched(this.profileForm);
    }
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
      if (field.errors['passwordMismatch']) {
        return 'Las contraseñas no coinciden';
      }
      if (field.errors['onlyLetters']) {
        return 'Solo se permiten letras';
      }
      if (field.errors['onlyNumbers']) {
        return 'Solo se permiten números, espacios, guiones y el símbolo +';
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

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    this.clearMessages();
    this.clear2FAMessages();
  }

  isActiveTab(tab: string): boolean {
    return this.activeTab === tab;
  }
}