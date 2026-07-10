import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, afterEach } from 'vitest';
import { VerifyEmail } from './verify-email';
import { AuthenticationAPIService } from '../../../api';

describe('VerifyEmail Component', () => {
  let component: VerifyEmail;
  let fixture: ComponentFixture<VerifyEmail>;
  let mockAuthApi: { verifyEmail: ReturnType<typeof vi.fn> };

  const setupTest = (token: string | null) => {
    mockAuthApi = {
      verifyEmail: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [VerifyEmail],
      providers: [
        provideRouter([{ path: 'login', redirectTo: '' }]),
        { provide: AuthenticationAPIService, useValue: mockAuthApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: () => token
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyEmail);
    component = fixture.componentInstance;
  };

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('should show success message and redirect on valid token using isolated fake timers', () => {
    setupTest('valid-token');
    vi.useFakeTimers();
    mockAuthApi.verifyEmail.mockReturnValue(of(null));

    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    fixture.detectChanges();

    expect(mockAuthApi.verifyEmail).toHaveBeenCalledWith('valid-token');
    expect(component['isSuccess']()).toBe(true);

    const successMessage = fixture.nativeElement.querySelector('.success-message');
    expect(successMessage).toBeTruthy();

    vi.advanceTimersByTime(5000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should show error message on invalid token signatures', () => {
    setupTest('invalid-token');
    mockAuthApi.verifyEmail.mockReturnValue(throwError(() => ({ status: 400, error: { detail: 'Invalid token' } })));

    fixture.detectChanges();

    expect(component['isSuccess']()).toBe(false);
    expect(component['errorMessage']()).toContain('Invalid token');

    const errorMessage = fixture.nativeElement.querySelector('.error-message');
    expect(errorMessage).toBeTruthy();
  });

  it('should short-circuit and show error message if no token is present in the route snapshot', () => {
    setupTest(null);
    mockAuthApi.verifyEmail.mockReturnValue(of(null));

    fixture.detectChanges();

    expect(mockAuthApi.verifyEmail).not.toHaveBeenCalled();
    expect(component['errorMessage']()).toBe('No verification token found.');
  });
});
