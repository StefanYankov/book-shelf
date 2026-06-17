import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './services/auth.service';

/**
 * Functional Route Guard that prevents non-admin users from accessing admin routes.
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // if (authService.isLoggedIn() && authService.hasRole('ROLE_ADMIN')) {
  //   return true;
  // }

  // For now, just redirect to home if not an admin
  return router.createUrlTree(['/']);
};
