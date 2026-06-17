import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { AuthService } from '../../../core/services/auth.service';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Component } from '@angular/core';

@Component({ standalone: true, template: '' })
class DummyHomeComponent {}

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'app/home', component: DummyHomeComponent }
        ]),
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when component is created', () => {
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('should have a valid form when username and password are provided', () => {
    component.loginForm.controls['username'].setValue('testuser');
    component.loginForm.controls['password'].setValue('password');
    expect(component.loginForm.valid).toBeTruthy();
  });

  it('should disable the login button when the form is invalid', () => {
    const button = fixture.nativeElement.querySelector('button');
    expect(button.disabled).toBeTruthy();
  });

  it('should enable the login button when the form is valid', async () => {
    component.loginForm.controls['username'].setValue('testuser');
    component.loginForm.controls['password'].setValue('password');
    fixture.detectChanges();
    await fixture.whenStable();
    const button = fixture.nativeElement.querySelector('button');
    expect(button.disabled).toBeFalsy();
  });

  it('should call authService.login on submit with valid form', () => {
    const loginSpy = vi.spyOn(authService, 'login').mockReturnValue(of({ token: 'fake-token' }));

    component.loginForm.controls['username'].setValue('testuser');
    component.loginForm.controls['password'].setValue('password');

    component.onSubmit();

    expect(loginSpy).toHaveBeenCalledWith({ username: 'testuser', password: 'password' });
  });

  it('should set errorMessage on 401 error', async () => {
    const errorDetail = 'Invalid username or password.';
    vi.spyOn(authService, 'login').mockReturnValue(throwError(() => ({ status: 401, error: { detail: errorDetail } })));

    component.loginForm.controls['username'].setValue('testuser');
    component.loginForm.controls['password'].setValue('password');

    component.onSubmit();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.errorMessage).toBe(errorDetail);
    const errorDiv = fixture.nativeElement.querySelector('.error-message');
    expect(errorDiv.textContent).toContain(errorDetail);
  });
});
