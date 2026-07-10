import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ForgotPassword } from './forgot-password';
import { AuthenticationAPIService } from '../../../api';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { of, throwError } from 'rxjs';

describe('ForgotPassword Component', () => {
  let component: ForgotPassword;
  let fixture: ComponentFixture<ForgotPassword>;
  let mockAuthApi: { forgotPassword: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAuthApi = {
      forgotPassword: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ForgotPassword],
      providers: [
        provideRouter([]),
        { provide: AuthenticationAPIService, useValue: mockAuthApi }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPassword);
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

  it('should trigger control marking validation highlights on invalid blank form submissions', () => {
    component.onSubmit();
    fixture.detectChanges();

    expect(component['forgotPasswordForm'].get('email')?.touched).toBeTruthy();
    const validationError = fixture.nativeElement.querySelector('.validation-error');
    expect(validationError).toBeTruthy();
  });

  it('should call the forgotPassword API on valid submit', () => {
    mockAuthApi.forgotPassword.mockReturnValue(of(null));

    component['forgotPasswordForm'].controls['email'].setValue('test@example.com');
    component.onSubmit();

    expect(mockAuthApi.forgotPassword).toHaveBeenCalledWith({ email: 'test@example.com' });
  });

  it('should display success message and redirect on success via fake timer loops', () => {
    vi.useFakeTimers();
    mockAuthApi.forgotPassword.mockReturnValue(of(null));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    component['forgotPasswordForm'].controls['email'].setValue('test@example.com');
    component.onSubmit();
    fixture.detectChanges();

    expect(component['isSuccess']()).toBe(true);

    vi.advanceTimersByTime(4000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should capture failure payloads and map them to error messages safely', () => {
    const errorMsg = 'Invalid domain target.';
    mockAuthApi.forgotPassword.mockReturnValue(throwError(() => ({ error: { detail: errorMsg } })));

    component['forgotPasswordForm'].controls['email'].setValue('test@example.com');
    component.onSubmit();
    fixture.detectChanges();

    expect(component['errorMessage']()).toBe(errorMsg);
    const errorDiv = fixture.nativeElement.querySelector('.error-message');
    expect(errorDiv.textContent).toContain(errorMsg);
  });
});
