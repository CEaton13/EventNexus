import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { VenueResponse } from '../../../shared/models/venue.model';

/**
 * AdminVenueList displays all venues for the active org within the admin layout.
 * Admins can create, edit, and delete venues without leaving the admin shell.
 */
@Component({
  selector: 'app-admin-venue-list',
  standalone: false,
  templateUrl: './admin-venue-list.html',
  styleUrl: './admin-venue-list.scss',
})
export class AdminVenueList implements OnInit {
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);

  constructor(
    private readonly venueService: VenueService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  /**
   * Loads all venues for the active organization.
   */
  load(): void {
    this.loading.set(true);
    this.venueService.getAll().subscribe({
      next: (venues) => {
        this.venues.set(venues);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  /**
   * Navigates to the venue creation form.
   */
  create(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'venues', 'new']);
  }

  /**
   * Navigates to the venue edit form.
   * @param id Venue primary key.
   */
  edit(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'venues', id, 'edit']);
  }

  /**
   * Opens a confirm dialog then deletes the venue on confirmation.
   * @param venue The venue to delete.
   */
  confirmDelete(venue: VenueResponse): void {
    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Delete Venue',
          message: `Delete "${venue.name}"? This cannot be undone.`,
          confirmLabel: 'Delete',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.venueService.delete(venue.id).subscribe({
          next: () => {
            this.snackBar.open('Venue deleted.', 'OK', { duration: 3000 });
            this.load();
          },
          error: () => {
            this.snackBar.open('Failed to delete venue.', 'OK', { duration: 4000 });
          },
        });
      });
  }
}
