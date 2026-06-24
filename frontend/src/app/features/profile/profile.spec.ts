import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Profile } from './profile';
import { UserAPIService } from '../../api';
import { ToastService } from '../../core/services/toast.service';
import { UserProfile } from '../../core/models/user-profile.model';

describe('Profile Component', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockUserApiService: { getMyProfile: ReturnType<typeof vi.fn>; updateMyProfile: ReturnType<typeof vi.fn>; changeMyPassword: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn> };

  const mockProfile: UserProfile = {
    id: 'user-123',
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User'
  };

  beforeEach(async () => {
    mockUserApiService = {
      getMyProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateMyProfile: vi.fn().mockReturnValue(of(undefined)),
      changeMyPassword: vi.fn().mockReturnValue(of(undefined))
    };
    mockToastService = {
      showSuccess: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserAPIService, useValue: mockUserApiService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and fetch profile on init', () => {
    expect(component).toBeTruthy();
    expect(mockUserApiService.getMyProfile).toHaveBeenCalled();
    expect(component.profileForm.value.firstName).toBe('Test');
  });

  it('should call updateMyProfile on profile form submit', () => {
    component.profileForm.controls['firstName'].setValue('Updated');
    component.onProfileSubmit();
    expect(mockUserApiService.updateMyProfile).toHaveBeenCalledWith({
      firstName: 'Updated',
      lastName: 'User'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should call changeMyPassword on password form submit', () => {
    component.passwordForm.controls['currentPassword'].setValue('oldPass');
    component.passwordForm.controls['newPassword'].setValue('newStrongPassword');
    fixture.detectChanges();
    component.onPasswordSubmit();
    expect(mockUserApiService.changeMyPassword).toHaveBeenCalledWith({
      currentPassword: 'oldPass',
      newPassword: 'newStrongPassword'
    });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });
});
