import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { AdminProfile } from './admin-profile';
import { UserProfileAPIService } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';

describe('AdminProfile Component Tests', () => {
  let component: AdminProfile;
  let fixture: ComponentFixture<AdminProfile>;

  let mockUserProfileAPIService: { changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let mockAuthService: { logout: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    // Arrange (shared): mock collaborators; password change returns a token we intentionally ignore.
    mockUserProfileAPIService = {
      changeMyPassword: vi.fn().mockReturnValue(of({ token: 'new-admin-token' }))
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };
    mockAuthService = {
      logout: vi.fn()
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
    // Arrange & Act (construction in beforeEach)

    // Assert
    expect(component).toBeTruthy();
    expect(component.passwordForm.invalid).toBe(true);
  });

  it('should evaluate form as invalid when password rules are violated', () => {
    // Arrange
    component.passwordForm.patchValue({
      currentPassword: 'adminPassword123!',
      newPassword: 'weak',
      confirmPassword: 'weak'
    });

    // Act
    fixture.detectChanges();

    // Assert
    expect(component.passwordForm.invalid).toBe(true);
    expect(component.passwordForm.controls.newPassword.errors?.['hasNumber']).toBe(true);
    expect(component.passwordForm.controls.newPassword.errors?.['hasSpecial']).toBe(true);
  });

  it('should evaluate form as invalid when password mismatch occurs', () => {
    // Arrange
    component.passwordForm.patchValue({
      currentPassword: 'adminPassword123!',
      newPassword: 'StrongPassword123!',
      confirmPassword: 'MismatchedPassword123!'
    });

    // Act
    fixture.detectChanges();

    // Assert
    expect(component.passwordForm.invalid).toBe(true);
    expect(component.passwordForm.errors?.['passwordMismatch']).toBe(true);
  });

  it('should change password then log the admin out to force re-authentication', async () => {
    // Arrange
    component.passwordForm.patchValue({
      currentPassword: 'oldPassword123!',
      newPassword: 'newStrongAdminPassword123!',
      confirmPassword: 'newStrongAdminPassword123!'
    });

    // Act
    component.onPasswordSubmit();
    await stabilizeState();

    // Assert
    expect(mockUserProfileAPIService['changeMyPassword']).toHaveBeenCalledWith({
      currentPassword: 'oldPassword123!',
      newPassword: 'newStrongAdminPassword123!'
    });
    expect(mockToastService['showSuccess']).toHaveBeenCalled();
    expect(mockAuthService['logout']).toHaveBeenCalledTimes(1);
  });

  it('should show an error toast and NOT log out when the password change fails', async () => {
    // Arrange
    mockUserProfileAPIService.changeMyPassword.mockReturnValue(
      throwError(() => ({ error: { detail: 'Current password incorrect' } }))
    );
    component.passwordForm.patchValue({
      currentPassword: 'wrongOld123!',
      newPassword: 'newStrongAdminPassword123!',
      confirmPassword: 'newStrongAdminPassword123!'
    });

    // Act
    component.onPasswordSubmit();
    await stabilizeState();

    // Assert
    expect(mockToastService['showError']).toHaveBeenCalledWith('Current password incorrect');
    expect(mockAuthService['logout']).not.toHaveBeenCalled();
  });
});
