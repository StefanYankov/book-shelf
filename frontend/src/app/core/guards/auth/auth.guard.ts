import { inject } from '@angular/core';
import { Router, CanActivateFn, UrlTree, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Structural guard checking authentication status and forcing role-aware password rotation routes.
 */
export const authGuard: CanActivateFn = (_route, state: RouterStateSnapshot): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  // Enforce password rotation detour bounds across distinct layout trees
  if (authService.isPasswordChangeRequired()) {
    const isAdmin = authService.userRole() === 'ROLE_ADMIN';
    const targetProfilePath = isAdmin ? '/admin/profile' : '/app/profile';

    if (state.url !== targetProfilePath) {
      return router.createUrlTree([targetProfilePath]);
    }
  }

  return true;
};
