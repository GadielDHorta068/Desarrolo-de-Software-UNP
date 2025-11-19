import { Component, OnInit, OnDestroy, ElementRef, ViewChild, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LoadingIndicator } from '../../shared/components/loading-indicator/loading-indicator';
import { AuthService, LoginRequest } from '../../services/auth.service';
import { fadeInUp, inputFocus } from '../../animations/route-animations';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingIndicator],
  templateUrl: './login.html',
  styleUrl: './login.css',
  animations: [fadeInUp, inputFocus]
})
export class Login implements OnInit, OnDestroy {
  @ViewChild('mascot', { static: false }) mascot!: ElementRef;
  
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  twoFARequired = false;
  
  // Propiedades para la mascota
  mascotX = 50;
  mascotY = 50;
  targetX = 50;
  targetY = 50;
  animationId: number | null = null;
  
  // Estados de focus para animaciones
  emailFocused = false;
  passwordFocused = false;

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
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      otp: [''],
      recoveryCode: ['']
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginRequest: LoginRequest = this.loginForm.value;

      if (this.twoFARequired) {
        const otp = this.loginForm.get('otp')?.value as string;
        const recovery = this.loginForm.get('recoveryCode')?.value as string;
        if ((!otp || otp.length !== 6) && (!recovery || recovery.length === 0)) {
          this.isLoading = false;
          this.errorMessage = 'Ingresa el código 2FA de 6 dígitos o un código de respaldo';
          return;
        }
        loginRequest.otp = otp && otp.length === 6 ? otp : undefined;
        loginRequest.recoveryCode = recovery && recovery.length > 0 ? recovery.trim() : undefined;
      }

      this.authService.login(loginRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isLoading = false;
          if (error?.status === 401 && error?.error?.requires2FA) {
            this.twoFARequired = true;
            this.errorMessage = 'Tu cuenta tiene 2FA habilitado. Ingresa el código 2FA o un código de respaldo.';
          } else {
            this.errorMessage = 'Credenciales inválidas. Por favor, intenta de nuevo.';
          }
          console.error('Login error:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName === 'email' ? 'Email' : 'Contraseña'} es requerido`;
      }
      if (field.errors['email']) {
        return 'Email inválido';
      }
      if (field.errors['minlength']) {
        return 'La contraseña debe tener al menos 6 caracteres';
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
  
  onEmailFocus(): void {
    this.emailFocused = true;
  }
  
  onEmailBlur(): void {
    this.emailFocused = false;
  }
  
  onPasswordFocus(): void {
    this.passwordFocused = true;
  }
  
  onPasswordBlur(): void {
    this.passwordFocused = false;
  }
}
