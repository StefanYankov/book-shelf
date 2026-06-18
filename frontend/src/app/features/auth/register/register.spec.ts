import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { Register } from './register';
import { AuthService } from '../../../core/services/auth.service';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { of, throwError } from 'rxjs';

describe('Register Component', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when created', () => {
    expect(component.registerForm.valid).toBeFalsy();
  });

  it('should call authService.register on valid submit', () => {
    const registerSpy = vi.spyOn(authService, 'register').mockReturnValue(of({ token: 'fake-token' }));

    component.registerForm.setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();

    expect(registerSpy).toHaveBeenCalled();
  });

  it('should display success message and redirect on successful registration', async () => {
    vi.useFakeTimers();
    vi.spyOn(authService, 'register').mockReturnValue(of({ token: 'fake-token' }));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    component.registerForm.setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isSuccess).toBe(true);
    const successMessage = fixture.nativeElement.querySelector('.success-message');
    expect(successMessage).toBeTruthy();

    vi.advanceTimersByTime(4000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should display error message on 409 conflict', async () => {
    vi.spyOn(authService, 'register').mockReturnValue(throwError(() => ({ status: 409, error: { detail: 'Username taken' } })));

    component.registerForm.setValue({
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'password123'
    });

    component.onSubmit();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.errorMessage).toBe('Username taken');
    const errorMessage = fixture.nativeElement.querySelector('.error-message');
    expect(errorMessage.textContent).toContain('Username taken');
  });
});
