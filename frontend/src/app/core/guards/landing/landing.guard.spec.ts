import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { landingGuard } from './landing.guard';
import { AuthService } from '../../services/auth.service';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('landingGuard Unit Tests', () => {
  let mockAuthService: Record<string, ReturnType<typeof vi.fn>>;
  let mockRouter: Record<string, ReturnType<typeof vi.fn>>;
  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

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

  it('should pass transparently when user is not logged in', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() => landingGuard(dummyRoute, dummyState));

    expect(result).toBe(true);
    expect(mockRouter['createUrlTree']).not.toHaveBeenCalled();
  });

  it('should route standard users directly to the user home dashboard', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(false);
    mockAuthService['userRole'].mockReturnValue('ROLE_USER');

    const result = TestBed.runInInjectionContext(() => landingGuard(dummyRoute, dummyState));

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/app/home']);
    expect(result).toEqual(['/app/home']);
  });

  it('should route administrative accounts directly to the admin site root', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(false);
    mockAuthService['userRole'].mockReturnValue('ROLE_ADMIN');

    const result = TestBed.runInInjectionContext(() => landingGuard(dummyRoute, dummyState));

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/admin']);
    expect(result).toEqual(['/admin']);
  });

  it('should redirect user to app profile if password modification is pending', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_USER');

    const result = TestBed.runInInjectionContext(() => landingGuard(dummyRoute, dummyState));

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/app/profile']);
    expect(result).toEqual(['/app/profile']);
  });

  it('should redirect admin to admin profile if password modification is pending', () => {
    mockAuthService['isLoggedIn'].mockReturnValue(true);
    mockAuthService['isPasswordChangeRequired'].mockReturnValue(true);
    mockAuthService['userRole'].mockReturnValue('ROLE_ADMIN');

    const result = TestBed.runInInjectionContext(() => landingGuard(dummyRoute, dummyState));

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/admin/profile']);
    expect(result).toEqual(['/admin/profile']);
  });
});
