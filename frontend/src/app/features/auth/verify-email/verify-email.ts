import { Component, inject, OnInit, DestroyRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthenticationAPIService } from '../../../api';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css'
})
export class VerifyEmail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthenticationAPIService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly isLoading = signal(true);
  protected readonly isSuccess = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading.set(false);
      this.errorMessage.set('No verification token found.');
      return;
    }

    this.authApi.verifyEmail(token)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.isSuccess.set(true);

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 5000);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.isSuccess.set(false);
          this.errorMessage.set(
            err.error?.detail || 'Failed to verify email. The link may be invalid or expired.'
          );
        }
      });
  }
}
