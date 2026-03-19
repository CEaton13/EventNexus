import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { TenantService } from '../services/tenant.service';

/**
 * orgGuard — validates that the authenticated user belongs to the org
 * identified by the `:orgSlug` route parameter and sets it as the active org.
 *
 * Usage in route definition:
 * ```ts
 * {
 *   path: ':orgSlug',
 *   canActivate: [authGuard, orgGuard],
 *   children: [...]
 * }
 * ```
 *
 * Redirects to /unauthorized if the user is not a member of the requested org.
 */
export const orgGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const tenantService = inject(TenantService);
  const router = inject(Router);

  const slug = route.paramMap.get('orgSlug');
  if (!slug) {
    return router.createUrlTree(['/unauthorized']);
  }

  const isMember = tenantService.memberships().some(m => m.organizationSlug === slug);
  if (!isMember) {
    return router.createUrlTree(['/unauthorized']);
  }

  tenantService.selectOrg(slug);
  return true;
};
