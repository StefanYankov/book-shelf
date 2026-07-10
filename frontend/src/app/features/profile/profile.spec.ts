import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Profile } from './profile';
import { UserAPIService, AuthenticationResponse } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { UserProfile } from '../../core/models/user-profile.model';
import { AuthService } from '../../core/services/auth.service';

describe('Profile Component', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockUserApiService: { getMyProfile: ReturnType<typeof vi.fn>; updateMyProfile: ReturnType<typeof vi.fn>; changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn> };
  let mockAuthService: { login: ReturnType<typeof vi.fn>; isPasswordChangeRequired: ReturnType<typeof vi.fn> };

  const mockProfile: UserProfile = {
    id: 'user-123',
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User'
  };

  const mockAuthResponse: AuthenticationResponse = { token: 'new-mock-token' };

  beforeEach(async () => {
    mockUserApiService = {
      getMyProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateMyProfile: vi.fn().mockReturnValue(of(undefined)),
      changeMyPassword: vi.fn().mockReturnValue(of(mockAuthResponse))
    };
    mockToastService = {
      showSuccess: vi.fn()
    };
    mockAuthService = {
      login: vi.fn().mockReturnValue(of(undefined)),
      isPasswordChangeRequired: vi.fn().mockReturnValue(false)
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserAPIService, useValue: mockUserApiService },
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
    expect(mockUserApiService.getMyProfile).toHaveBeenCalled();
    expect(component.profileForm.value.firstName).toBe('Test');
  });

  it('should call updateMyProfile on profile form submit', async () => {
    component.profileForm.controls['firstName'].setValue('Updated');
    component.onProfileSubmit();
    await stabilizeState();
    expect(mockUserApiService.updateMyProfile).toHaveBeenCalledWith({
      firstName: 'Updated',
      lastName: 'User'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should call changeMyPassword and update auth state on password form submit', async () => {
    component.passwordForm.controls['currentPassword'].setValue('oldPass');
    component.passwordForm.controls['newPassword'].setValue('newStrongPassword');
    component.onPasswordSubmit();
    await stabilizeState();
    expect(mockUserApiService.changeMyPassword).toHaveBeenCalledWith({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword'
    });
    expect(mockAuthService.login).toHaveBeenCalled();
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });
});
