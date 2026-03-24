import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * LandingComponent is the public home page.
 * Authenticated users are redirected to their org's tournament list.
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
    if (this.authService.isAuthenticated()) {
      const slug =
        this.tenantService.currentOrgSlug() ??
        this.tenantService.memberships()[0]?.organizationSlug;
      if (slug) {
        this.router.navigate([slug, 'tournaments']);
      }
    }
  }
}