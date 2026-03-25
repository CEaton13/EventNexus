import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from './core/services/auth';
import { TenantService } from './core/services/tenant.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss'
})
export class App {
  readonly authService = inject(AuthService);
  readonly tenantService = inject(TenantService);

  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);

  /** True when the user belongs to at least one org (used for org-scoped nav links). */
  readonly hasOrg = computed(() => this.tenantService.currentOrgSlug() !== null);

  /** Current org slug — used to build nav links. */
  readonly orgSlug = computed(() => this.tenantService.currentOrgSlug());

  /** Logs out and navigates to the landing page. */
  onLogout(): void {
    this.authService.logout().subscribe({
      complete: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/'])
    });
  }

  /** Navigates to the admin dashboard of the active org. */
  goToDashboard(): void {
    const slug = this.orgSlug();
    if (slug) this.router.navigate([slug, 'admin', 'dashboard']);
  }

  /** Navigates to the tournament list for the active org. */
  goToTournaments(): void {
    const slug = this.orgSlug();
    if (slug) this.router.navigate([slug, 'tournaments']);
    else this.router.navigate(['/tournaments']);
  }

  /** Opens the auth dialog (login tab). Imported lazily to avoid circular dep at boot. */
  async openAuthDialog(): Promise<void> {
    const { AuthDialog } = await import('./shared/components/auth-dialog/auth-dialog');
    this.dialog.open(AuthDialog, {
      width: '440px',
      panelClass: 'dark-dialog',
      disableClose: false,
      data: { initialTab: 0 }
    });
  }
}
