import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VenueService } from '../../../core/services/venue.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { VenueResponse } from '../../../shared/models/venue.model';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';

/**
 * VenueDetailComponent shows a venue's details including live station utilization.
 * Admins can edit or delete the venue from this page.
 */
@Component({
  selector: 'app-venue-detail',
  standalone: false,
  templateUrl: './venue-detail.html',
  styleUrl: './venue-detail.scss',
})
export class VenueDetail implements OnInit {
  readonly venue = signal<VenueResponse | null>(null);
  readonly loading = signal(false);

  private venueId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly venueService: VenueService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.venueId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.venueService.getById(this.venueId).subscribe({
      next: v => {
        this.venue.set(v);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  editVenue(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'venues', this.venueId, 'edit']);
  }

  deleteVenue(): void {
    const name = this.venue()?.name ?? 'this venue';
    this.dialog.open(ConfirmDialog, {
      panelClass: 'dark-dialog',
      data: {
        title: 'Delete Venue',
        message: `Permanently delete "${name}"? This cannot be undone.`,
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
      },
    }).afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.venueService.delete(this.venueId).subscribe({
        next: () => {
          this.snackBar.open('Venue deleted', 'OK', { duration: 3000 });
          this.backToList();
        },
        error: () => this.snackBar.open('Cannot delete — venue is in use', 'OK', { duration: 4000 }),
      });
    });
  }

  backToList(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'venues']);
  }
}
