import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

/**
 * Functional Route Guard that prevents non-admin users from accessing admin routes.
 */
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  // For now, just redirect to home if not an admin
  return router.createUrlTree(['/']);
};
