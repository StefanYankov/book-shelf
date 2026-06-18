import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationAPIService } from '../../../api';
import { ResetPasswordRequest } from '../../../api';
import { matchPasswordValidator } from '../../../shared/validators/match-password.validator';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword {
  private authApi = inject(AuthenticationAPIService);
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  resetPasswordForm = this.fb.group({
    token: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  }, {
    validators: matchPasswordValidator('newPassword', 'confirmPassword')
  });

  errorMessage: string | null = null;
  isLoading = false;
  isSuccess = false;

  constructor() {
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        this.resetPasswordForm.controls['token'].setValue(params['token']);
      }
    });
  }

  onSubmit(): void {
    if (this.resetPasswordForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;

      const request: ResetPasswordRequest = {
        token: this.resetPasswordForm.value.token!,
        newPassword: this.resetPasswordForm.value.newPassword!
      };

      this.authApi.resetPassword(request).pipe(
        tap(() => {
          this.isLoading = false;
          this.isSuccess = true;
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        }),
        catchError(err => {
          this.isLoading = false;
          if (err.status === 400) {
            this.errorMessage = err.error?.detail || 'Invalid or expired token.';
          } else {
            this.errorMessage = 'An unexpected error occurred. Please try again.';
          }
          return of(null);
        })
      ).subscribe();
    }
  }
}
