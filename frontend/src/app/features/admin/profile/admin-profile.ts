import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { UserProfileAPIService, ChangePasswordDto, AuthenticationResponse } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { PasswordRule } from '../../../core/models/password-rule.model';
import { ValidationConstants } from '../../../core/constants/validation.constants';

@Component({
  selector: 'app-admin-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-profile.html',
  styleUrl: './admin-profile.css',
})
export class AdminProfile {
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

  /**
   * Commits the administrative credentials update workflow and switches session contexts cleanly.
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
        this.toastService.showSuccess('Password updated successfully.');
        this.passwordForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.toastService.showError(err.error?.detail || 'Failed to update administrative password.');
      }
    });
  }

  /**
   * Computes the reactive string evaluation of the target password input field.
   */
  protected get newPasswordValue(): string {
    return this.passwordForm.controls.newPassword.value || '';
  }

  /**
   * Validates if the minimal absolute length criteria condition passes evaluation bounds.
   */
  protected get isMinLengthValid(): boolean {
    return this.newPasswordValue.length >= 8;
  }

  /**
   * Ascertains if a single rule mapping constraints passes regex verification metrics.
   */
  protected isRuleValid(ruleKey: string): boolean {
    const control = this.passwordForm.get('newPassword');
    if (!control || !control.value) return false;
    return !control.errors?.[ruleKey];
  }

  // Analyzes character complexity criteria strings to dynamically populate inline reactive form validation state mappings.
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

  // Ensures strict equivalence assertions between the newly suggested credential string and the second verification field.
  private passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    if (!newPassword || !confirmPassword) return null;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }
}
