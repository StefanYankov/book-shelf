import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../api';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm = inject(FormBuilder).group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  errorMessage: string | null = null;
  isLoading = false;
  isSuccess = false;

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;

      const request: RegisterRequest = this.registerForm.value as RegisterRequest;

      this.authService.register(request).pipe(
        tap(() => {
          this.isLoading = false;
          this.isSuccess = true;

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 4000);
        }),
        catchError(err => {
          this.isLoading = false;
          if (err.status === 409) {
            this.errorMessage = err.error.detail || 'Username or email is already taken.';
          } else {
            this.errorMessage = 'An unexpected error occurred. Please try again.';
          }
          return of(null);
        })
      ).subscribe();
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
}
