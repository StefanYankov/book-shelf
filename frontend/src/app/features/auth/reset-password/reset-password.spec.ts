import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { ResetPassword } from './reset-password';
import { AuthenticationAPIService } from '../../../api';
import { vi, afterEach, beforeEach, describe, it, expect } from 'vitest';
import { of, throwError } from 'rxjs';

describe('ResetPassword Component', () => {
  let component: ResetPassword;
  let fixture: ComponentFixture<ResetPassword>;
  let mockAuthApi: { resetPassword: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAuthApi = {
      resetPassword: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ResetPassword],
      providers: [
        provideRouter([{ path: 'login', redirectTo: '' }]), // Provide routing path fallback loops
        { provide: AuthenticationAPIService, useValue: mockAuthApi }, // Strict isolation provider bridge
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
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('should create cleanly', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when component is initialized', () => {
    expect(component['resetPasswordForm'].valid).toBeFalsy();
  });

  it('should populate token from query params on init', () => {
    expect(component['resetPasswordForm'].controls.token.value).toBe('test-token-from-url');
  });

  const fillValidForm = () => {
    component['resetPasswordForm'].controls.token.setValue('valid-token');
    component['resetPasswordForm'].controls.newPassword.setValue('newPassword123');
    component['resetPasswordForm'].controls.confirmPassword.setValue('newPassword123');
  };

  it('should trigger control marking validation highlights on invalid submissions', () => {
    component.onSubmit();
    fixture.detectChanges();

    expect(component['resetPasswordForm'].get('newPassword')?.touched).toBeTruthy();
    const validationErrors = fixture.nativeElement.querySelectorAll('.validation-error');
    expect(validationErrors.length).toBeGreaterThan(0);
  });

  it('should call the resetPassword API on valid submit', () => {
    mockAuthApi.resetPassword.mockReturnValue(of(null));

    fillValidForm();
    component.onSubmit();

    expect(mockAuthApi.resetPassword).toHaveBeenCalled();
  });

  it('should set isSuccess to true and navigate on successful reset using isolated fake timers', () => {
    vi.useFakeTimers();
    mockAuthApi.resetPassword.mockReturnValue(of(null));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    fillValidForm();
    component.onSubmit();
    fixture.detectChanges();

    // Check Signal outputs cleanly as functional queries
    expect(component['isSuccess']()).toBe(true);

    vi.advanceTimersByTime(3000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should set errorMessage signal on API response failures', () => {
    const errorDetail = 'Invalid token provided by mock';
    mockAuthApi.resetPassword.mockReturnValue(throwError(() => ({ status: 400, error: { detail: errorDetail } })));

    fillValidForm();
    component.onSubmit();
    fixture.detectChanges();

    expect(component['errorMessage']()).toBe(errorDetail);
    const errorDiv = fixture.nativeElement.querySelector('.error-message');
    expect(errorDiv.textContent).toContain(errorDetail);
  });
});
