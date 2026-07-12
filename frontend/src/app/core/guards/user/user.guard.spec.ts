import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { userGuard } from './user.guard';
import { AuthService } from '../../services/auth.service';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('userGuard Unit Tests', () => {
  let mockAuthService: Record<string, ReturnType<typeof vi.fn>>;
  let mockRouter: Record<string, ReturnType<typeof vi.fn>>;
  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  beforeEach(() => {
    mockAuthService = {
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

  it('should allow access for regular users', () => {
    mockAuthService['userRole'].mockReturnValue('ROLE_USER');

    const result = TestBed.runInInjectionContext(() => userGuard(dummyRoute, dummyState));

    expect(result).toBe(true);
    expect(mockRouter['createUrlTree']).not.toHaveBeenCalled();
  });

  it('should block administrators and bounce them back to the admin portal root', () => {
    mockAuthService['userRole'].mockReturnValue('ROLE_ADMIN');

    const result = TestBed.runInInjectionContext(() => userGuard(dummyRoute, dummyState));

    expect(mockRouter['createUrlTree']).toHaveBeenCalledWith(['/admin']);
    expect(result).toEqual(['/admin']);
  });
});
