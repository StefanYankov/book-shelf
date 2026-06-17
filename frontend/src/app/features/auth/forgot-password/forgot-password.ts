import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationAPIService } from '../../../api';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  private authApi = inject(AuthenticationAPIService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  forgotPasswordForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  errorMessage: string | null = null;
  isLoading = false;
  isSuccess = false;

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;

      this.authApi.forgotPassword(this.forgotPasswordForm.value as { email: string }).pipe(
        tap(() => {
          this.isLoading = false;
          this.isSuccess = true;

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 4000);
        }),
        catchError(_ => {
          this.isLoading = false;
          this.errorMessage = 'An unexpected error occurred. Please try again.';
          return of(null);
        })
      ).subscribe();
    }
  }
}
