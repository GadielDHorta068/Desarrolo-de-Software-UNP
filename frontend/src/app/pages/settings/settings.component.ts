import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
  activeTab = 'profile';

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

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    this.clearMessages();
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
}