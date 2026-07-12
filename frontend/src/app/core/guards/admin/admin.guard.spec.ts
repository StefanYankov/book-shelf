import { TestBed } from '@angular/core/testing';
import { CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from '../../services/auth.service';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  let mockAuthService: { userRole: ReturnType<typeof vi.fn> };
  let mockRouter: { createUrlTree: ReturnType<typeof vi.fn> };

  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  beforeEach(() => {
    mockAuthService = {
      userRole: vi.fn()
    };
    mockRouter = {
      createUrlTree: vi.fn().mockReturnValue('/app/home')
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

  it('should allow access for ROLE_ADMIN', async () => {
    mockAuthService.userRole.mockReturnValue('ROLE_ADMIN');

    const canActivate = executeGuard(dummyRoute, dummyState);

    await new Promise(resolve => setTimeout(resolve, 0));

    expect(canActivate).toBe(true);
  });

  it('should redirect to home for non-admin user roles', async () => {
    mockAuthService.userRole.mockReturnValue('ROLE_USER');

    const canActivate = executeGuard(dummyRoute, dummyState);

    await new Promise(resolve => setTimeout(resolve, 0));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/app/home']);
    expect(canActivate).not.toBe(true);
  });

  it('should handle unassigned null token states defensively and block entry', async () => {
    mockAuthService.userRole.mockReturnValue(null);

    const canActivate = executeGuard(dummyRoute, dummyState);

    await new Promise(resolve => setTimeout(resolve, 0));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/app/home']);
    expect(canActivate).not.toBe(true);
  });
});
