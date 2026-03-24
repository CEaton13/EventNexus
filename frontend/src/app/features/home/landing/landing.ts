import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * LandingComponent is the public home page.
 * Authenticated users are redirected based on their role and org membership.
 * TOURNAMENT_ADMINs with no org see a "Create your organization" CTA.
 */
@Component({
  selector: 'app-landing',
  standalone: false,
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
})
export class Landing implements OnInit {
  constructor(
    private readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) return;

    const slug =
      this.tenantService.currentOrgSlug() ?? this.tenantService.memberships()[0]?.organizationSlug;
    const role = this.authService.user()?.role;

    if (role === 'SPECTATOR') {
      this.router.navigate(['/tournaments']);
      return;
    }

    if (slug) {
      const dest =
        role === 'TOURNAMENT_ADMIN' ? [slug, 'admin', 'dashboard'] : [slug, 'tournaments'];
      this.router.navigate(dest);
    }
    // TOURNAMENT_ADMIN or TEAM_MANAGER with no org: stay on landing to show CTA.
  }

  /** True when an authenticated admin has no org yet — shows org creation CTA. */
  get showOrgCta(): boolean {
    return this.authService.isAdmin() && this.tenantService.memberships().length === 0;
  }
}
