import { TestBed, inject } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from '../auth.interceptor';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('Auth Interceptor Specifications', () => {
  let httpMock: HttpTestingController;
  let mockAuthService: { getToken: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockAuthService = { getToken: vi.fn() };
    mockRouter = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should append bearer tokens to outbound headers if token exists in session context', inject([HttpClient], (httpClient: HttpClient) => {
    mockAuthService.getToken.mockReturnValue('mock_jwt_string');

    httpClient.get('/api/resource').subscribe();

    const req = httpMock.expectOne('/api/resource');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock_jwt_string');
    req.flush({});
  }));

  it('should catch 403 password-change-required custom exceptions and redirect to profile views', inject([HttpClient], (httpClient: HttpClient) => {
    mockAuthService.getToken.mockReturnValue('mock_jwt_string');

    httpClient.get('/api/protected').subscribe({
      error: (err) => expect(err).toBeTruthy()
    });

    const req = httpMock.expectOne('/api/protected');
    req.flush(
      { type: 'urn:bookshelf:password-change-required' },
      { status: 403, statusText: 'Forbidden' }
    );

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/app/profile']);
  }));
});
