import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';
import { TenantService } from '../services/tenant.service';

/**
 * roleGuard — protects routes that require a specific role.
 *
 * For routes requiring `TOURNAMENT_ADMIN`, access is also granted when the
 * user holds `ORG_ADMIN` in the currently active organization (org-level admin).
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
  const tenantService = inject(TenantService);
  const router = inject(Router);

  const allowedRoles: string[] = route.data['roles'] ?? [];
  const userRole = authService.user()?.role;

  if (userRole && allowedRoles.includes(userRole)) {
    return true;
  }

  // Org admins may access routes restricted to TOURNAMENT_ADMIN.
  if (allowedRoles.includes('TOURNAMENT_ADMIN') && tenantService.isOrgAdmin()) {
    return true;
  }

  return router.createUrlTree(['/unauthorized']);
};
