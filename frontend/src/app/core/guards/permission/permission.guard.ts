import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';

/**
 * Functional guard factory that admits only users holding a specific authority (permission).
 * Unlike the role-based guards, this gates on a granted capability, enabling permission-delegated
 * routes (e.g. a MODERATE_BOOKS holder reaching the book moderation page) without granting a role.
 *
 * @param permission the exact authority string required, e.g. 'MODERATE_BOOKS'.
 * @returns a CanActivateFn that allows access when the current user holds the permission,
 *          otherwise redirects to the user home.
 */
export const permissionGuard = (permission: string): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasAuthority(permission)) {
      return true;
    }

    return router.createUrlTree(['/app/home']);
  };
};
