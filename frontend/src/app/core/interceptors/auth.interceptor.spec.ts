import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { signal, WritableSignal } from '@angular/core';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('authInterceptor Unit Tests', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  let mockAuthService: {
    getToken: ReturnType<typeof vi.fn>;
    userRole: WritableSignal<string | null>;
  };

  beforeEach(() => {
    mockAuthService = {
      getToken: vi.fn().mockReturnValue('mock-jwt-token'),
      userRole: signal<string | null>(null)
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
    vi.restoreAllMocks();
  });

  describe('Authorization Headers Mapping', () => {
    it('should inject Bearer token header into outgoing HTTP requests when token is present', () => {
      httpClient.get('/api/test-data').subscribe();

      const req = httpTestingController.expectOne('/api/test-data');
      expect(req.request.headers.has('Authorization')).toBe(true);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-jwt-token');
      req.flush({});
    });

    it('should pass request unmodified if no local token is present', () => {
      mockAuthService.getToken.mockReturnValue(null);

      httpClient.get('/api/test-data').subscribe();

      const req = httpTestingController.expectOne('/api/test-data');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });
  });
});
