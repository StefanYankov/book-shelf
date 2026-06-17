import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { ForgotPassword } from './forgot-password';
import { AuthenticationAPIService } from '../../../api';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { of } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

describe('ForgotPassword', () => {
  let component: ForgotPassword;
  let fixture: ComponentFixture<ForgotPassword>;
  let authApi: AuthenticationAPIService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForgotPassword],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthenticationAPIService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPassword);
    component = fixture.componentInstance;
    authApi = TestBed.inject(AuthenticationAPIService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call the forgotPassword API on valid submit', () => {
    const mockResponse = new HttpResponse<any>({ status: 200 });
    const forgotPasswordSpy = vi.spyOn(authApi, 'forgotPassword').mockReturnValue(of(mockResponse));
    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    component.onSubmit();
    expect(forgotPasswordSpy).toHaveBeenCalledWith({ email: 'test@example.com' });
  });

  it('should display success message and redirect on success', async () => {
    vi.useFakeTimers();
    const mockResponse = new HttpResponse<any>({ status: 200 });
    vi.spyOn(authApi, 'forgotPassword').mockReturnValue(of(mockResponse));
    const router = TestBed.inject(Router);
    const routerSpy = vi.spyOn(router, 'navigate');

    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    component.onSubmit();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isSuccess).toBe(true);

    vi.advanceTimersByTime(4000);
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });
});
