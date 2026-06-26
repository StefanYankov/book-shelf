import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
      TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  let mockAuthService: { userRole: () => string | null };
  let mockRouter: { createUrlTree: ReturnType<typeof vi.fn> };

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

  it('should allow access for ROLE_ADMIN', () => {
    (mockAuthService.userRole as ReturnType<typeof vi.fn>).mockReturnValue('ROLE_ADMIN');
    const canActivate = executeGuard({} as any, {} as any);
    expect(canActivate).toBe(true);
  });

  it('should redirect to home for non-admin roles', () => {
    (mockAuthService.userRole as ReturnType<typeof vi.fn>).mockReturnValue('ROLE_USER');
    const canActivate = executeGuard({} as any, {} as any);
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/app/home']);
    expect(canActivate).not.toBe(true);
  });
});
