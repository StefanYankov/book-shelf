import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserAPIService, UpdateProfileDto, ChangePasswordDto, AuthenticationResponse } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';
import { UserProfile } from '../../core/models/user-profile.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Profile implements OnInit {
  private readonly userApiService = inject(UserAPIService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  public readonly authService = inject(AuthService);

  profileForm = this.fb.nonNullable.group({
    username: [{ value: '', disabled: true }],
    email: [{ value: '', disabled: true }],
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
  });

  passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]]
  });

  ngOnInit(): void {
    this.userApiService.getMyProfile().subscribe((user: UserProfile) => {
      this.profileForm.patchValue(user);
    });
  }

  onProfileSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }
    const { firstName, lastName } = this.profileForm.getRawValue();
    const dto: UpdateProfileDto = { firstName, lastName };
    this.userApiService.updateMyProfile(dto).subscribe({
      next: () => {
        this.toastService.showSuccess('Profile updated successfully.');
      },
      error: (err: HttpErrorResponse) => {
        this.toastService.showError(err.error?.detail || 'Failed to update profile.');
      }
    });
  }

  onPasswordSubmit(): void {
    if (this.passwordForm.invalid) {
      return;
    }
    const rawValue = this.passwordForm.getRawValue();
    const dto: ChangePasswordDto = {
      currentPassword: rawValue.currentPassword || '',
      newPassword: rawValue.newPassword || ''
    };
    this.userApiService.changeMyPassword(dto).subscribe({
      next: (response: AuthenticationResponse) => {
        this.authService.login({} as any).subscribe(() => {
            this.toastService.showSuccess('Password changed successfully.');
            this.passwordForm.reset();
        });
      },
      error: (err: HttpErrorResponse) => {
        this.toastService.showError(err.error?.detail || 'Failed to change password.');
      }
    });
  }
}
