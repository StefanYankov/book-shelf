import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Register } from './register';
import { AuthService } from '../../../core/services/auth.service';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { of, throwError } from 'rxjs';

describe('Register Component', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let mockAuthService: { register: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAuthService = {
      register: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideRouter([{ path: 'login', redirectTo: '' }]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
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

  it('should map invalid controls on default state setups', () => {
    expect(component['registerForm'].valid).toBeFalsy();
  });

  it('should delegate operational registration payloads to the authentication core services', () => {
    mockAuthService.register.mockReturnValue(of({ token: 'fake-token' }));

    component['registerForm'].setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();

    expect(mockAuthService.register).toHaveBeenCalled();
  });

  it('should render functional success states and handle redirect sequences via fake timers', () => {
    vi.useFakeTimers();
    mockAuthService.register.mockReturnValue(of({ token: 'fake-token' }));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    component['registerForm'].setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();
    fixture.detectChanges();

    expect(component['isSuccess']()).toBe(true);
    const successMessage = fixture.nativeElement.querySelector('.success-message');
    expect(successMessage).toBeTruthy();

    vi.advanceTimersByTime(4000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should catch error collisions and expose them through signal slots', () => {
    mockAuthService.register.mockReturnValue(throwError(() => ({ status: 409, error: { detail: 'Username taken' } })));

    component['registerForm'].setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();
    fixture.detectChanges();

    expect(component['errorMessage']()).toBe('Username taken');
    const errorMessage = fixture.nativeElement.querySelector('.error-message');
    expect(errorMessage.textContent).toContain('Username taken');
  });
});
