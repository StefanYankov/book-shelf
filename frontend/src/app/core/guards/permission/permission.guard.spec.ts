import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {permissionGuard} from './permission.guard';
import {AuthService} from '../../services/auth.service';
import {beforeEach, describe, expect, it, vi} from 'vitest';

describe('permissionGuard Unit Tests', () => {
  let mockAuthService: { hasAuthority: ReturnType<typeof vi.fn> };
  let mockRouter: { createUrlTree: ReturnType<typeof vi.fn> };
  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  const runGuard = (permission: string): boolean | UrlTree => {
    const guard: CanActivateFn = permissionGuard(permission);
    return TestBed.runInInjectionContext(() => guard(dummyRoute, dummyState)) as boolean | UrlTree;
  };

  beforeEach(() => {
    mockAuthService = {hasAuthority: vi.fn()};
    mockRouter = {
      createUrlTree: vi.fn().mockImplementation((commands: string[]) => commands as unknown as UrlTree)
    };

    TestBed.configureTestingModule({
      providers: [
        {provide: AuthService, useValue: mockAuthService},
        {provide: Router, useValue: mockRouter}
      ]
    });
  });

  it('should allow access when the user holds the required permission', () => {
    // Arrange
    mockAuthService.hasAuthority.mockReturnValue(true);

    // Act
    const result = runGuard('MODERATE_BOOKS');

    // Assert
    expect(mockAuthService.hasAuthority).toHaveBeenCalledWith('MODERATE_BOOKS');
    expect(result).toBe(true);
    expect(mockRouter.createUrlTree).not.toHaveBeenCalled();
  });

  it('should redirect to the user home when the user lacks the permission', () => {
    // Arrange
    mockAuthService.hasAuthority.mockReturnValue(false);

    // Act
    const result = runGuard('MODERATE_BOOKS');

    // Assert
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/app/home']);
    expect(result).toEqual(['/app/home']);
  });
});
