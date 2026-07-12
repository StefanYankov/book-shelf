import { inject } from '@angular/core';
import { Router, UrlTree, CanActivateFn } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Guard that prevents authenticated users from accessing public landing/guest routes.
 * Redirects logged-in users to their respective portals based on role and password security status.
 */
export const landingGuard: CanActivateFn = (): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    if (authService.isPasswordChangeRequired()) {
      return authService.userRole() === 'ROLE_ADMIN'
        ? router.createUrlTree(['/admin/profile'])
        : router.createUrlTree(['/app/profile']);
    }

    return authService.userRole() === 'ROLE_ADMIN'
      ? router.createUrlTree(['/admin'])
      : router.createUrlTree(['/app/home']);
  }

  return true;
};
