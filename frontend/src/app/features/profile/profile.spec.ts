import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Profile } from './profile';
import { UserProfileAPIService, AuthenticationResponse } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { UserProfile } from '../../core/models/user-profile.model';
import { AuthService } from '../../core/services/auth.service';

describe('Profile Component', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockUserProfileAPIService: { getMyProfile: ReturnType<typeof vi.fn>; updateMyProfile: ReturnType<typeof vi.fn>; changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn> };
  let mockAuthService: { handleAuthenticationResponse: ReturnType<typeof vi.fn>; isPasswordChangeRequired: ReturnType<typeof vi.fn> };

  const mockProfile: UserProfile = {
    id: 'user-123',
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User'
  };

  const mockAuthResponse: AuthenticationResponse = { token: 'new-mock-token' };

  beforeEach(async () => {
    mockUserProfileAPIService = {
      getMyProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateMyProfile: vi.fn().mockReturnValue(of(undefined)),
      changeMyPassword: vi.fn().mockReturnValue(of(mockAuthResponse))
    };
    mockToastService = {
      showSuccess: vi.fn()
    };
    mockAuthService = {
      handleAuthenticationResponse: vi.fn(),
      isPasswordChangeRequired: vi.fn().mockReturnValue(false)
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
    expect(component).toBeTruthy();
    expect(mockUserProfileAPIService.getMyProfile).toHaveBeenCalled();
  });

  it('should call updateMyProfile on profile form submit', async () => {
    component.profileForm.patchValue({ firstName: 'Updated' });
    component.onProfileSubmit();
    await stabilizeState();
    expect(mockUserProfileAPIService.updateMyProfile).toHaveBeenCalledWith({
      firstName: 'Updated',
      lastName: 'User'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should call changeMyPassword and update auth state on password form submit', async () => {
    component.passwordForm.patchValue({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword1!',
      confirmPassword: 'newStrongPassword1!'
    });
    component.onPasswordSubmit();
    await stabilizeState();
    expect(mockUserProfileAPIService.changeMyPassword).toHaveBeenCalledWith({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword1!'
    });
    expect(mockAuthService.handleAuthenticationResponse).toHaveBeenCalledWith(mockAuthResponse);
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });
});
