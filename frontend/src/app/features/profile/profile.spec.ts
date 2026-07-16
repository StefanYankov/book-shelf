import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Profile } from './profile';
import { UserProfileAPIService } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { UserProfile } from '../../core/models/user-profile.model';
import { AuthService } from '../../core/services/auth.service';

describe('Profile Component', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockUserProfileAPIService: { getMyProfile: ReturnType<typeof vi.fn>; updateMyProfile: ReturnType<typeof vi.fn>; changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let mockAuthService: { logout: ReturnType<typeof vi.fn> };

  const mockProfile: UserProfile = {
    id: 'user-123',
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User'
  };

  beforeEach(async () => {
    // Arrange (shared): mock collaborators; password change returns a token we intentionally ignore.
    mockUserProfileAPIService = {
      getMyProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateMyProfile: vi.fn().mockReturnValue(of(undefined)),
      changeMyPassword: vi.fn().mockReturnValue(of({ token: 'new-mock-token' }))
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };
    mockAuthService = {
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserProfileAPIService, useValue: mockUserProfileAPIService },
        { provide: ToastService, useValue: mockToastService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const stabilizeState = async () => {
    fixture.detectChanges();
    await Promise.resolve();
  };

  it('should create and fetch profile on init', () => {
    // Arrange & Act (construction + ngOnInit in beforeEach)

    // Assert
    expect(component).toBeTruthy();
    expect(mockUserProfileAPIService.getMyProfile).toHaveBeenCalled();
  });

  it('should call updateMyProfile on profile form submit', async () => {
    // Arrange
    component.profileForm.patchValue({ firstName: 'Updated' });

    // Act
    component.onProfileSubmit();
    await stabilizeState();

    // Assert
    expect(mockUserProfileAPIService.updateMyProfile).toHaveBeenCalledWith({
      firstName: 'Updated',
      lastName: 'User'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should NOT log out on a profile (non-password) update', async () => {
    // Arrange
    component.profileForm.patchValue({ firstName: 'Updated' });

    // Act
    component.onProfileSubmit();
    await stabilizeState();

    // Assert: editing profile details must not end the session
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('should change password then log the user out to force re-authentication', async () => {
    // Arrange
    component.passwordForm.patchValue({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword1!',
      confirmPassword: 'newStrongPassword1!'
    });

    // Act
    component.onPasswordSubmit();
    await stabilizeState();

    // Assert
    expect(mockUserProfileAPIService.changeMyPassword).toHaveBeenCalledWith({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword1!'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
    expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
  });
});
