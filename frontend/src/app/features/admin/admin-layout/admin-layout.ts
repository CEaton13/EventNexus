import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';
import { TenantService } from '../../../core/services/tenant.service';
import { AuthService } from '../../../core/services/auth';

/**
 * AdminLayoutComponent is the shell for all admin routes under /:orgSlug/admin/.
 *
 * It renders a responsive sidebar navigation (MatSidenav) and a status bar.
 * On desktop the sidebar is pinned (mode="side"). On mobile it collapses to an
 * overlay that can be toggled via the hamburger button in the status bar.
 *
 * All admin child routes render inside the <router-outlet> in the main content area.
 */
@Component({
  selector: 'app-admin-layout',
  standalone: false,
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout implements OnInit, OnDestroy {
  /** True when the viewport matches a mobile breakpoint. */
  readonly isMobile = signal(false);

  private breakpointSub: Subscription | null = null;

  constructor(
    private readonly breakpointObserver: BreakpointObserver,
    readonly tenantService: TenantService,
    readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  /** Slug of the currently active org, used for absolute routerLink targets. */
  get orgSlug(): string | null {
    return this.tenantService.currentOrgSlug();
  }

  ngOnInit(): void {
    this.breakpointSub = this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.TabletPortrait])
      .subscribe((result) => {
        this.isMobile.set(result.matches);
      });
  }

  ngOnDestroy(): void {
    this.breakpointSub?.unsubscribe();
  }
}
