import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { AdminProfile } from './admin-profile';
import { UserProfileAPIService, AuthenticationResponse } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';

describe('AdminProfile Component Tests', () => {
  let component: AdminProfile;
  let fixture: ComponentFixture<AdminProfile>;

  let mockUserProfileAPIService: { changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let mockAuthService: { handleAuthenticationResponse: ReturnType<typeof vi.fn> };

  const mockAuthResponse: AuthenticationResponse = { token: 'new-admin-token' };

  beforeEach(async () => {
    mockUserProfileAPIService = {
      changeMyPassword: vi.fn().mockReturnValue(of(mockAuthResponse))
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };
    mockAuthService = {
      handleAuthenticationResponse: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AdminProfile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserProfileAPIService, useValue: mockUserProfileAPIService },
        { provide: ToastService, useValue: mockToastService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const stabilizeState = async () => {
    fixture.detectChanges();
    await Promise.resolve();
  };

  it('should initialize cleanly', () => {
    expect(component).toBeTruthy();
    expect(component.passwordForm.invalid).toBe(true);
  });

  it('should evaluate form as invalid when password rules are violated', () => {
    component.passwordForm.patchValue({
      currentPassword: 'adminPassword123!',
      newPassword: 'weak',
      confirmPassword: 'weak'
    });

    fixture.detectChanges();

    expect(component.passwordForm.invalid).toBe(true);
    expect(component.passwordForm.controls.newPassword.errors?.['hasNumber']).toBe(true);
    expect(component.passwordForm.controls.newPassword.errors?.['hasSpecial']).toBe(true);
  });

  it('should evaluate form as invalid when password mismatch occurs', () => {
    component.passwordForm.patchValue({
      currentPassword: 'adminPassword123!',
      newPassword: 'StrongPassword123!',
      confirmPassword: 'MismatchedPassword123!'
    });

    fixture.detectChanges();

    expect(component.passwordForm.invalid).toBe(true);
    expect(component.passwordForm.errors?.['passwordMismatch']).toBe(true);
  });

  it('should execute changeMyPassword API call on form submission when inputs are valid', async () => {
    component.passwordForm.patchValue({
      currentPassword: 'oldPassword123!',
      newPassword: 'newStrongAdminPassword123!',
      confirmPassword: 'newStrongAdminPassword123!'
    });

    component.onPasswordSubmit();
    await stabilizeState();

    expect(mockUserProfileAPIService['changeMyPassword']).toHaveBeenCalledWith({
      currentPassword: 'oldPassword123!',
      newPassword: 'newStrongAdminPassword123!'
    });
    expect(mockAuthService['handleAuthenticationResponse']).toHaveBeenCalledWith(mockAuthResponse);
    expect(mockToastService['showSuccess']).toHaveBeenCalledWith('Password updated successfully.');
  });
});
