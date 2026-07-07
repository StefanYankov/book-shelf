import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Functional Route Guard that prevents non-admin users from accessing admin routes.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.userRole() === 'ROLE_ADMIN') {
    return true;
  }

  return router.createUrlTree(['/app/home']);
};
