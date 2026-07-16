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

  // --- Helper to re-create the service with a token already in storage ---
  // Mirrors the constructor-seeding path (authToken is initialized from storage).
  function createServiceWithStoredToken(token: string): AuthService {
    localStorage.setItem(TOKEN_KEY, token);
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthenticationAPIService, useValue: mockAuthApiService },
        { provide: Router, useValue: mockRouter }
      ]
    });
    return TestBed.inject(AuthService);
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
      expect(service.getToken()).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
      expect(service.userRole()).toBeNull();
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

    it('should not throw when setItem fails during login (write path degrades silently)', () => {
      // Arrange
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('Storage quota exceeded');
      });
      const token = createFakeToken({ sub: 'u', role: 'ROLE_USER', exp: 9999999999 });
      mockAuthApiService.authenticate.mockReturnValue(of({ token }));

      // Act & Assert: the in-memory signal still updates even though persistence failed
      expect(() => service.login({ username: 'u', password: 'p' }).subscribe()).not.toThrow();
      expect(service.getToken()).toBe(token);
    });
  });

  describe('JWT Decoding and Computed Signal Tests', () => {
    it('should correctly decode valid claims and evaluate computed user role and active state', () => {
      // Arrange
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600;
      const fakeToken = createFakeToken({
        sub: 'john_doe',
        role: 'ROLE_ADMIN',
        exp: futureExpiry
      });

      // Act
      const activeService = createServiceWithStoredToken(fakeToken);

      // Assert
      expect(activeService.getToken()).toBe(fakeToken);
      expect(activeService.userRole()).toBe('ROLE_ADMIN');
      expect(activeService.isLoggedIn()).toBe(true);
    });

    it('should evaluate isLoggedIn to false if token is expired', () => {
      // Arrange
      const pastExpiry = Math.floor(Date.now() / 1000) - 3600;
      const fakeToken = createFakeToken({
        sub: 'john_doe',
        role: 'ROLE_USER',
        exp: pastExpiry
      });

      // Act
      const expiredService = createServiceWithStoredToken(fakeToken);

      // Assert
      expect(expiredService.isLoggedIn()).toBe(false);
    });
  });

  describe('userId Claim Decoding', () => {
    it('should expose the userId claim from a valid token', () => {
      // Arrange
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600;
      const fakeToken = createFakeToken({
        sub: 'john_doe',
        role: 'ROLE_USER',
        userId: '22222222-0000-0000-0000-000000000001',
        exp: futureExpiry
      });

      // Act
      const activeService = createServiceWithStoredToken(fakeToken);

      // Assert
      expect(activeService.userId()).toBe('22222222-0000-0000-0000-000000000001');
    });

    it('should return null userId when no token is present', () => {
      expect(service.userId()).toBeNull();
    });

    it('should return null userId when the claim is absent from the token', () => {
      // Arrange: valid token but WITHOUT a userId claim
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600;
      const fakeToken = createFakeToken({ sub: 'legacy', role: 'ROLE_USER', exp: futureExpiry });

      // Act
      const legacyService = createServiceWithStoredToken(fakeToken);

      // Assert
      expect(legacyService.userId()).toBeNull();
    });
  });

  describe('Password Change Required Claim Decoding', () => {
    it('should evaluate isPasswordChangeRequired to true when the claim is set', () => {
      // Arrange
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600;
      const fakeToken = createFakeToken({
        sub: 'admin',
        role: 'ROLE_ADMIN',
        pwd_chg_req: true,
        exp: futureExpiry
      });

      // Act
      const rotationService = createServiceWithStoredToken(fakeToken);

      // Assert
      expect(rotationService.isPasswordChangeRequired()).toBe(true);
    });

    it('should default isPasswordChangeRequired to false when the claim is absent', () => {
      // Arrange: token WITHOUT pwd_chg_req (simulates an older/edge token)
      const futureExpiry = Math.floor(Date.now() / 1000) + 3600;
      const fakeToken = createFakeToken({ sub: 'user1', role: 'ROLE_USER', exp: futureExpiry });

      // Act
      const defaultService = createServiceWithStoredToken(fakeToken);

      // Assert: the `|| false` fallback must hold
      expect(defaultService.isPasswordChangeRequired()).toBe(false);
    });
  });

  describe('Malformed Token Resilience', () => {
    it('should degrade all computed signals to safe defaults when the token is corrupt', () => {
      // Arrange: a syntactically broken JWT (decodeToken's try/catch should swallow it)
      const garbageToken = 'not.a.valid-jwt';

      // Act
      const brokenService = createServiceWithStoredToken(garbageToken);

      // Assert: no throw, everything falls back cleanly
      expect(brokenService.isLoggedIn()).toBe(false);
      expect(brokenService.userRole()).toBeNull();
      expect(brokenService.userId()).toBeNull();
      expect(brokenService.isPasswordChangeRequired()).toBe(false);
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

    it('should NOT store a token when the auth response contains no token', () => {
      // Arrange: backend responded 200 but with an empty body/token
      mockAuthApiService.authenticate.mockReturnValue(of({} as { token?: string }));

      // Act
      service.login({ username: 'u', password: 'p' }).subscribe();

      // Assert: state stays logged-out, nothing persisted
      expect(service.getToken()).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
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

  describe('Expired Token Identity Suppression (regression)', () => {
    // Guards the bug where an expired-but-present token still leaked userId/role,
    // causing action buttons (e.g. review delete) to render for a logged-out user.
    it('should suppress userId, userRole, and pwd_chg_req when the token is expired', () => {
      // Arrange: a token that decodes fine but is past expiry, for an ADMIN
      const pastExpiry = Math.floor(Date.now() / 1000) - 3600;
      const expiredAdminToken = createFakeToken({
        sub: 'admin',
        role: 'ROLE_ADMIN',
        userId: '11111111-0000-0000-0000-000000000001',
        pwd_chg_req: true,
        exp: pastExpiry,
      });

      // Act
      const expiredService = createServiceWithStoredToken(expiredAdminToken);

      // Assert: logged out, and NO identity leaks through
      expect(expiredService.isLoggedIn()).toBe(false);
      expect(expiredService.userId()).toBeNull();
      expect(expiredService.userRole()).toBeNull();
      expect(expiredService.isPasswordChangeRequired()).toBe(false);
    });
  });
});
