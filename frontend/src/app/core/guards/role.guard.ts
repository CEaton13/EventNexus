import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * roleGuard — protects routes that require a specific role.
 *
 * Usage in route definition:
 * ```ts
 * {
 *   path: 'admin',
 *   canActivate: [authGuard, roleGuard],
 *   data: { roles: ['TOURNAMENT_ADMIN'] },
 *   ...
 * }
 * ```
 *
 * Redirects to /unauthorized when the user's role is not in the allowed list.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles: string[] = route.data['roles'] ?? [];
  const userRole = authService.user()?.role;

  if (userRole && allowedRoles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/unauthorized']);
};
