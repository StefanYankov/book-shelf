import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthenticationRequest } from '../../../api';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  errorMessage = signal<string | null>(null);
  isLoading = signal(false);
  isAccountDisabled = signal(false);

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.isAccountDisabled.set(false);

    const credentials = this.loginForm.value as AuthenticationRequest;

    this.authService.login(credentials).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/app/home']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 403 && err.error?.type === 'urn:bookshelf:account-disabled') {
          this.isAccountDisabled.set(true);
          this.errorMessage.set(err.error.detail);
        } else {
          this.errorMessage.set(err.error?.detail || 'An unknown error occurred during login.');
        }
      }
    });
  }
}
