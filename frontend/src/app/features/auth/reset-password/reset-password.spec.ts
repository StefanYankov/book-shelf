import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { ResetPassword } from './reset-password';
import { AuthenticationAPIService } from '../../../api/api/authenticationAPI.service';
import { vi, afterEach, beforeEach, describe, it, expect } from 'vitest';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

describe('ResetPassword', () => {
  let component: ResetPassword;
  let fixture: ComponentFixture<ResetPassword>;
  let authApi: AuthenticationAPIService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPassword],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthenticationAPIService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ token: 'test-token-from-url' })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPassword);
    component = fixture.componentInstance;
    authApi = TestBed.inject(AuthenticationAPIService);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when component is created', () => {
    expect(component.resetPasswordForm.valid).toBeFalsy();
  });

  it('should populate token from query params on init', async () => {
    await fixture.whenStable();
    expect(component.resetPasswordForm.controls['token'].value).toBe('test-token-from-url');
  });

  // Helper to make the form valid
  const fillValidForm = () => {
    component.resetPasswordForm.controls['token'].setValue('valid-token');
    component.resetPasswordForm.controls['newPassword'].setValue('newPassword123');
    component.resetPasswordForm.controls['confirmPassword'].setValue('newPassword123');
  };

  it('should call the resetPassword API on valid submit', () => {
    const mockResponse = new HttpResponse<any>({ status: 200 });
    const resetPasswordSpy = vi.spyOn(authApi, 'resetPassword').mockReturnValue(of(mockResponse));

    fillValidForm();
    component.onSubmit();

    expect(resetPasswordSpy).toHaveBeenCalled();
  });

  it('should set isSuccess to true and navigate on successful reset', async () => {
    vi.useFakeTimers();
    const mockResponse = new HttpResponse<any>({ status: 200 });
    vi.spyOn(authApi, 'resetPassword').mockReturnValue(of(mockResponse));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    fillValidForm();
    component.onSubmit();

    expect(component.isSuccess).toBe(true);

    vi.advanceTimersByTime(3000);

    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should set errorMessage on API error', async () => {
    const errorDetail = 'Invalid token provided by mock';
    vi.spyOn(authApi, 'resetPassword').mockReturnValue(throwError(() => ({ status: 400, error: { detail: errorDetail } })));

    fillValidForm();
    component.onSubmit();

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.errorMessage).toBe(errorDetail);
  });
});
