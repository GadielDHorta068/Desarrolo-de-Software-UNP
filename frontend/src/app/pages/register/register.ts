import { Component, OnInit, OnDestroy, ElementRef, ViewChild, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, RegisterRequest } from '../../services/auth.service';
import { fadeInUp, inputFocus } from '../../animations/route-animations';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
  animations: [fadeInUp, inputFocus]
})
export class Register implements OnInit, OnDestroy {
  @ViewChild('mascot', { static: false }) mascot!: ElementRef;
  
  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;
  
  // Propiedades para la mascota
  mascotX = 50;
  mascotY = 50;
  targetX = 50;
  targetY = 50;
  animationId: number | null = null;
  
  // Estados de focus para animaciones
  focusedField = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.startMascotAnimation();
  }
  
  ngOnDestroy(): void {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
  }

  private initForm(): void {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      surname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      cellphone: [''],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    if (confirmPassword?.hasError('passwordMismatch')) {
      delete confirmPassword.errors!['passwordMismatch'];
      if (Object.keys(confirmPassword.errors!).length === 0) {
        confirmPassword.setErrors(null);
      }
    }
    
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const { confirmPassword, ...registerData } = this.registerForm.value;
      const registerRequest: RegisterRequest = registerData;

      this.authService.register(registerRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isLoading = false;
          // Usar el mensaje específico del backend si está disponible
          this.errorMessage = error.userMessage || 'Error al registrar usuario. Por favor, intenta de nuevo.';
          console.error('Register error:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }


  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.registerForm.controls).forEach(key => {
      const control = this.registerForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.registerForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        const fieldLabels: { [key: string]: string } = {
          name: 'Nombre',
          surname: 'Apellido',
          email: 'Email',
          cellphone: 'Teléfono',
          nickname: 'Nickname',
          password: 'Contraseña',
          confirmPassword: 'Confirmar contraseña'
        };
        return `${fieldLabels[fieldName]} es requerido`;
      }
      if (field.errors['email']) {
        return 'Email inválido';
      }
      if (field.errors['minlength']) {
        const minLength = field.errors['minlength'].requiredLength;
        return `Debe tener al menos ${minLength} caracteres`;
      }
      if (field.errors['passwordMismatch']) {
        return 'Las contraseñas no coinciden';
      }
    }
    return '';
  }
  
  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent): void {
    const rect = (event.currentTarget as Element).getBoundingClientRect();
    this.targetX = ((event.clientX - rect.left) / rect.width) * 100;
    this.targetY = ((event.clientY - rect.top) / rect.height) * 100;
  }
  
  private startMascotAnimation(): void {
    const animate = () => {
      // Suavizar el movimiento de la mascota
      const easing = 0.1;
      this.mascotX += (this.targetX - this.mascotX) * easing;
      this.mascotY += (this.targetY - this.mascotY) * easing;
      
      this.animationId = requestAnimationFrame(animate);
    };
    animate();
  }
  
  onFieldFocus(fieldName: string): void {
    this.focusedField = fieldName;
  }
  
  onFieldBlur(): void {
    this.focusedField = '';
  }
  
  isFieldFocused(fieldName: string): boolean {
    return this.focusedField === fieldName;
  }
}
