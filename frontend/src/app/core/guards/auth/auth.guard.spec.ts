import { TestBed } from '@angular/core/testing';
import { CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../../services/auth.service';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('authGuard Unit Tests', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  let mockAuthService: {
    isLoggedIn: ReturnType<typeof vi.fn>;
    isPasswordChangeRequired: ReturnType<typeof vi.fn>;
    userRole: ReturnType<typeof vi.fn>;
  };
  let mockRouter: { createUrlTree: ReturnType<typeof vi.fn> };

  const dummyRoute = {} as ActivatedRouteSnapshot;

  beforeEach(() => {
    mockAuthService = {
      isLoggedIn: vi.fn(),
      isPasswordChangeRequired: vi.fn(),
      userRole: vi.fn()
    };
    mockRouter = {
      createUrlTree: vi.fn().mockImplementation((commands: string[]) => commands as unknown as UrlTree)
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should allow access for logged-in users when no password change is required', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(false);
    const dummyState = { url: '/app/home' } as RouterStateSnapshot;

    const canActivate = executeGuard(dummyRoute, dummyState);

    expect(canActivate).toBe(true);
  });

  it('should redirect to login for logged-out sessions', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(false);
    const dummyState = { url: '/app/home' } as RouterStateSnapshot;

    const canActivate = executeGuard(dummyRoute, dummyState);

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/login']);
    expect(canActivate).not.toBe(true);
  });

  it('should force standard users to /app/profile when password rotation is required', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_USER');
    const dummyState = { url: '/app/home' } as RouterStateSnapshot;

    const result = executeGuard(dummyRoute, dummyState);

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/app/profile']);
    expect(result).toEqual(['/app/profile']);
  });

  it('should allow standard users to proceed if they are already navigating to their profile terminal', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_USER');
    const dummyState = { url: '/app/profile' } as RouterStateSnapshot;

    const result = executeGuard(dummyRoute, dummyState);

    expect(result).toBe(true);
    expect(mockRouter['createUrlTree']).not.toHaveBeenCalled();
  });

  it('should force administrative users to /admin/profile when password rotation is required', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_ADMIN');
    const dummyState = { url: '/admin/users' } as RouterStateSnapshot;

    const result = executeGuard(dummyRoute, dummyState);

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/admin/profile']);
    expect(result).toEqual(['/admin/profile']);
  });

  it('should allow administrative users to proceed if they are already navigating to their admin security terminal', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_ADMIN');
    const dummyState = { url: '/admin/profile' } as RouterStateSnapshot;

    const result = executeGuard(dummyRoute, dummyState);

    expect(result).toBe(true);
    expect(mockRouter['createUrlTree']).not.toHaveBeenCalled();
  });
});
