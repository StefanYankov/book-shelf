import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Prevents administrators from entering regular user views.
 */
export const userGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.userRole() === 'ROLE_ADMIN') {
    return router.createUrlTree(['/admin']);
  }

  return true;
};
