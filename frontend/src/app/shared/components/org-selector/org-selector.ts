import { Component } from '@angular/core';
import { TenantService } from '../../../core/services/tenant.service';
import { OrganizationMembership } from '../../models/organization.model';

/**
 * OrgSelectorComponent renders a dropdown that lets users switch between
 * the organizations they belong to.
 *
 * The component is hidden when the user belongs to only one organization.
 * Changing the selection calls `TenantService.selectOrg()` which persists
 * the choice to localStorage.
 *
 * Place in the app navbar:
 * ```html
 * <app-org-selector></app-org-selector>
 * ```
 */
@Component({
  selector: 'app-org-selector',
  standalone: false,
  templateUrl: './org-selector.html',
})
export class OrgSelector {
  constructor(readonly tenantService: TenantService) {}

  /** True when the user has more than one org — selector is hidden otherwise. */
  get hasMultipleOrgs(): boolean {
    return this.tenantService.memberships().length > 1;
  }

  /** The slug of the currently active org. */
  get selectedSlug(): string | null {
    return this.tenantService.currentOrgSlug();
  }

  /** All memberships available for selection. */
  get memberships(): OrganizationMembership[] {
    return this.tenantService.memberships();
  }

  /** Called by the mat-select (change) event. */
  onOrgChange(slug: string): void {
    this.tenantService.selectOrg(slug);
  }
}
