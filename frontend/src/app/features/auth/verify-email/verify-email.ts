import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, tap } from 'rxjs';
import { AuthenticationAPIService } from '../../../api';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css'
})
export class VerifyEmail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authApi = inject(AuthenticationAPIService);

  isLoading = true;
  isSuccess = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading = false;
      this.errorMessage = 'No verification token found.';
      return;
    }

    this.authApi.verifyEmail(token).pipe(
      tap(() => {
        this.isLoading = false;
        this.isSuccess = true;
        setTimeout(() => this.router.navigate(['/login']), 5000);
      }),
      catchError(err => {
        this.isLoading = false;
        this.isSuccess = false;
        this.errorMessage = err.error?.detail || 'Failed to verify email. The link may be invalid or expired.';
        return of(null);
      })
    ).subscribe();
  }
}
