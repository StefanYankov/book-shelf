import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { UserProfileAPIService, UpdateProfileDto, ChangePasswordDto, AuthenticationResponse } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';
import { UserProfile } from '../../core/models/user-profile.model';
import { AuthService } from '../../core/services/auth.service';
import { PasswordRule } from '../../core/models/password-rule.model';
import { ValidationConstants } from '../../core/constants/validation.constants';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly userProfileApiService = inject(UserProfileAPIService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  public readonly authService = inject(AuthService);

  protected readonly passwordRules: PasswordRule[] = [
    { key: 'hasNumber', regex: /\d/, message: 'At least one number (0-9).' },
    { key: 'hasSpecial', regex: /[@$!%*?&]/, message: 'At least one special character (@$!%*?&).' },
    { key: 'hasUpper', regex: /[A-Z]/, message: 'At least one uppercase letter (A-Z).' },
    { key: 'hasLower', regex: /[a-z]/, message: 'At least one lowercase letter (a-z).' }
  ];

  profileForm = this.fb.nonNullable.group({
    username: [{ value: '', disabled: true }],
    email: [{ value: '', disabled: true }],
    firstName: ['', [Validators.required, Validators.minLength(ValidationConstants.USER_FIRST_NAME_MIN_LENGTH), Validators.maxLength(ValidationConstants.USER_FIRST_NAME_MAX_LENGTH)]],
    lastName: ['', [Validators.required, Validators.minLength(ValidationConstants.USER_LAST_NAME_MIN_LENGTH), Validators.maxLength(ValidationConstants.USER_LAST_NAME_MAX_LENGTH)]]
  });

  passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [
      Validators.required,
      Validators.minLength(ValidationConstants.USER_PASSWORD_MIN_LENGTH),
      Validators.maxLength(ValidationConstants.USER_PASSWORD_MAX_LENGTH),
      this.createAgilePasswordValidator()
    ]],
    confirmPassword: ['', Validators.required]
  }, { validators: this.passwordMatchValidator });

  ngOnInit(): void {
    this.userProfileApiService.getMyProfile().subscribe((user: UserProfile) => {
      this.profileForm.patchValue(user);
    });
  }

  onProfileSubmit(): void {
    if (this.profileForm.invalid) return;
    const { firstName, lastName } = this.profileForm.getRawValue();
    const dto: UpdateProfileDto = { firstName, lastName };
    this.userProfileApiService.updateMyProfile(dto).subscribe({
      next: () => this.toastService.showSuccess('Profile updated successfully.'),
      error: (err: HttpErrorResponse) => this.toastService.showError(err.error?.detail || 'Failed to update profile.')
    });
  }

  /**
   * Commits the security password update workflow and updates the authentication identity payload.
   */
  onPasswordSubmit(): void {
    if (this.passwordForm.invalid) return;
    const rawValue = this.passwordForm.getRawValue();
    const dto: ChangePasswordDto = {
      currentPassword: rawValue.currentPassword || '',
      newPassword: rawValue.newPassword || ''
    };
    this.userProfileApiService.changeMyPassword(dto).subscribe({
      next: (response: AuthenticationResponse) => {
        this.authService.handleAuthenticationResponse(response);
        this.toastService.showSuccess('Password changed successfully.');
        this.passwordForm.reset();
      },
      error: (err: HttpErrorResponse) => this.toastService.showError(err.error?.detail || 'Failed to change password.')
    });
  }

  /**
   * Computes the current plain text string value of the new password field.
   */
  protected get newPasswordValue(): string {
    return this.passwordForm.controls.newPassword.value || '';
  }

  /**
   * Assesses if a specific password constraint regex criteria passes validation.
   */
  protected isRuleValid(ruleKey: string): boolean {
    const control = this.passwordForm.get('newPassword');
    if (!control || !control.value) return false;
    return !control.errors?.[ruleKey];
  }

  // Evaluates string input character arrays against rule regex criteria to build custom form validation flags.
  private createAgilePasswordValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const val = control.value || '';
      if (!val) return null;
      const errors: ValidationErrors = {};
      for (const rule of this.passwordRules) {
        if (!rule.regex.test(val)) {
          errors[rule.key] = true;
        }
      }
      return Object.keys(errors).length ? errors : null;
    };
  }

  // Cross-checks equality match conditions between the primary password input field and the confirmation input block.
  private passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    if (!newPassword || !confirmPassword) return null;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }
}
