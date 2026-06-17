import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { VerifyEmail } from './verify-email';
import { AuthenticationAPIService } from '../../../api';
import { HttpResponse } from '@angular/common/http';

describe('VerifyEmail Component', () => {
  let component: VerifyEmail;
  let fixture: ComponentFixture<VerifyEmail>;
  let authApi: AuthenticationAPIService;

  const setupTest = (token: string | null) => {
    TestBed.configureTestingModule({
      imports: [VerifyEmail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthenticationAPIService,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => token
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyEmail);
    component = fixture.componentInstance;
    authApi = TestBed.inject(AuthenticationAPIService);
  };

  it('should show success message and redirect on valid token', async () => {
    setupTest('valid-token');
    vi.useFakeTimers();
    const verifySpy = vi.spyOn(authApi, 'verifyEmail').mockReturnValue(of(new HttpResponse({ status: 200 })));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(verifySpy).toHaveBeenCalledWith('valid-token');
    expect(component.isSuccess).toBe(true);
    const successMessage = fixture.nativeElement.querySelector('.success-message');
    expect(successMessage).toBeTruthy();

    vi.advanceTimersByTime(5000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should show error message on invalid token', async () => {
    setupTest('invalid-token');
    vi.spyOn(authApi, 'verifyEmail').mockReturnValue(throwError(() => ({ status: 400, error: { detail: 'Invalid token' } })));

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isSuccess).toBe(false);
    expect(component.errorMessage).toContain('Invalid token');
    const errorMessage = fixture.nativeElement.querySelector('.error-message');
    expect(errorMessage).toBeTruthy();
  });

  it('should show error message if no token is present', async () => {
    setupTest(null);
    const verifySpy = vi.spyOn(authApi, 'verifyEmail');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(verifySpy).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('No verification token found.');
  });
});
