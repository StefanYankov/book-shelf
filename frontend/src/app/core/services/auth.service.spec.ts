import { TestBed } from '@angular/core/testing';
import { AuthService, DecodedToken } from './auth.service';
import { AuthenticationAPIService } from '../../api';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

// --- Safe Polyfill for Node/Headless Test Runners Missing LocalStorage ---
if (typeof globalThis.localStorage === 'undefined') {
  let store: Record<string, string> = {};
  const mockStorage = {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
    length: 0
  };
  Object.defineProperty(globalThis, 'localStorage', {
    value: mockStorage,
    writable: true
  });
}

describe('AuthService Unit Tests', () => {
  let service: AuthService;
  let mockAuthApiService: {
    authenticate: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
  };
  let mockRouter: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const TOKEN_KEY = 'bookshelf_jwt';

  // --- Helper to Generate Well-Formed Mock Base64 JWTs ---
  function createFakeToken(payload: Partial<DecodedToken>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payloadStr = btoa(JSON.stringify(payload));
    const signature = 'fake-signature-part';
    return `${header}.${payloadStr}.${signature}`;
  }

  beforeEach(() => {
    localStorage.clear();

    mockAuthApiService = {
      authenticate: vi.fn(),
      register: vi.fn()
    };

    mockRouter = {
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthenticationAPIService, useValue: mockAuthApiService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  describe('Initial State and Safe Storage Wrapper Tests', () => {
    it('should initialize with null state when no token is present in local storage', () => {
      // Arrange & Act
      const token = service.getToken();
      const isLoggedIn = service.isLoggedIn();
      const userRole = service.userRole();

      // Assert
      expect(token).toBeNull();
      expect(isLoggedIn).toBe(false);
      expect(userRole).toBeNull();
    });

    it('should degrade gracefully and handle localStorage exceptions inside safe wrappers', () => {
      // Arrange
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('Storage Blocked (Simulating Private Browsing)');
      });

      // Act
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          AuthService,
          { provide: AuthenticationAPIService, useValue: mockAuthApiService },
          { provide: Router, useValue: mockRouter }
        ]
      });
      const localService = TestBed.inject(AuthService);

      // Assert
      expect(localService.getToken()).toBeNull();
    });
  });

  describe('JWT Decoding and Computed Signal Tests', () => {
    it('should correctly decode valid claims and evaluate computed user role and active state', () => {
      // Arrange
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600; // 1 hour in the future
      const fakeToken = createFakeToken({
        sub: 'john_doe',
        role: 'ROLE_ADMIN',
        exp: futureExpiry
      });

      // Act
      localStorage.setItem(TOKEN_KEY, fakeToken);
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          AuthService,
          { provide: AuthenticationAPIService, useValue: mockAuthApiService },
          { provide: Router, useValue: mockRouter }
        ]
      });
      const activeService = TestBed.inject(AuthService);

      // Assert
      expect(activeService.getToken()).toBe(fakeToken);
      expect(activeService.userRole()).toBe('ROLE_ADMIN');
      expect(activeService.isLoggedIn()).toBe(true);
    });

    it('should evaluate isLoggedIn to false if token is expired', () => {
      // Arrange
      const pastExpiry = Math.floor(Date.now() / 1000) - 3600; // 1 hour in the past
      const fakeToken = createFakeToken({
        sub: 'john_doe',
        role: 'ROLE_USER',
        exp: pastExpiry
      });

      // Act
      localStorage.setItem(TOKEN_KEY, fakeToken);
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          AuthService,
          { provide: AuthenticationAPIService, useValue: mockAuthApiService },
          { provide: Router, useValue: mockRouter }
        ]
      });
      const expiredService = TestBed.inject(AuthService);

      // Assert
      expect(expiredService.isLoggedIn()).toBe(false);
    });
  });

  describe('Login & Registration Actions', () => {
    it('should store JWT token and update reactive state signals on successful login', () => {
      // Arrange
      const credentials = { username: 'testuser', password: 'password123' };
      const expectedToken = createFakeToken({ sub: 'testuser', role: 'ROLE_USER', exp: 9999999999 });
      mockAuthApiService.authenticate.mockReturnValue(of({ token: expectedToken }));

      // Act
      service.login(credentials).subscribe();

      // Assert
      expect(mockAuthApiService.authenticate).toHaveBeenCalledWith(credentials);
      expect(localStorage.getItem(TOKEN_KEY)).toBe(expectedToken);
      expect(service.getToken()).toBe(expectedToken);
      expect(service.isLoggedIn()).toBe(true);
      expect(service.userRole()).toBe('ROLE_USER');
    });

    it('should store JWT token and update reactive state signals on successful registration', () => {
      // Arrange
      const request = { firstName: 'John', lastName: 'Doe', email: 'john@example.com', username: 'john', password: 'password123' };
      const expectedToken = createFakeToken({ sub: 'john', role: 'ROLE_USER', exp: 9999999999 });
      mockAuthApiService.register.mockReturnValue(of({ token: expectedToken }));

      // Act
      service.register(request).subscribe();

      // Assert
      expect(mockAuthApiService.register).toHaveBeenCalledWith(request);
      expect(localStorage.getItem(TOKEN_KEY)).toBe(expectedToken);
      expect(service.getToken()).toBe(expectedToken);
    });
  });

  describe('Logout Routine', () => {
    it('should clear stored token, reset reactive signals, and navigate back to login route', () => {
      // Arrange
      const fakeToken = createFakeToken({ sub: 'john', role: 'ROLE_USER', exp: 9999999999 });
      localStorage.setItem(TOKEN_KEY, fakeToken);
      service['authToken'].set(fakeToken);

      // Act
      service.logout();

      // Assert
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
      expect(service.getToken()).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
      expect(service.userRole()).toBeNull();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });
});
