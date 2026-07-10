import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { authGuard } from '../auth.guard';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
      TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  let mockAuthService: { isLoggedIn: () => boolean };
  let mockRouter: { createUrlTree: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockAuthService = {
      isLoggedIn: vi.fn()
    };
    mockRouter = {
      createUrlTree: vi.fn().mockReturnValue('/login')
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  it('should allow access for logged-in users', () => {
    (mockAuthService.isLoggedIn as ReturnType<typeof vi.fn>).mockReturnValue(true);
    const canActivate = executeGuard({} as any, {} as any);
    expect(canActivate).toBe(true);
  });

  it('should redirect to login for logged-out users', () => {
    (mockAuthService.isLoggedIn as ReturnType<typeof vi.fn>).mockReturnValue(false);
    const canActivate = executeGuard({} as any, {} as any);
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(canActivate).not.toBe(true);
  });
});
