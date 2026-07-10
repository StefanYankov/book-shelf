import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { AuthService } from '../../../core/services/auth.service';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let mockAuthService: { login: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAuthService = {
      login: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([
          { path: 'app/home', redirectTo: '' }
        ]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create cleanly', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form state when component is initialized', () => {
    expect(component['loginForm'].valid).toBeFalsy();
  });

  it('should validate form constraints when username and password fields are filled', () => {
    component['loginForm'].controls['username'].setValue('testuser');
    component['loginForm'].controls['password'].setValue('password');
    expect(component['loginForm'].valid).toBeTruthy();
  });

  it('should trigger control marking validation highlights on invalid blank form submissions', () => {
    // Act
    component.onSubmit();
    fixture.detectChanges();

    // Assert
    expect(component['loginForm'].get('username')?.touched).toBeTruthy();
    expect(component['loginForm'].get('password')?.touched).toBeTruthy();

    const validationErrors = fixture.nativeElement.querySelectorAll('.validation-error');
    expect(validationErrors.length).toBe(2);
  });

  it('should call authService.login on submit with strictly typed form payloads', () => {
    mockAuthService.login.mockReturnValue(of({ token: 'fake-token' }));

    component['loginForm'].controls['username'].setValue('testuser');
    component['loginForm'].controls['password'].setValue('password');

    component.onSubmit();

    expect(mockAuthService.login).toHaveBeenCalledWith({ username: 'testuser', password: 'password' });
  });

  it('should render explicit error messages inside template views on response failures', () => {
    const errorDetail = 'Invalid username or password.';
    mockAuthService.login.mockReturnValue(throwError(() => ({ status: 401, error: { detail: errorDetail } })));

    component['loginForm'].controls['username'].setValue('testuser');
    component['loginForm'].controls['password'].setValue('password');

    component.onSubmit();
    fixture.detectChanges();

    expect(component['errorMessage']()).toBe(errorDetail);
    const errorDiv = fixture.nativeElement.querySelector('.error-message');
    expect(errorDiv.textContent).toContain(errorDetail);
  });
});
