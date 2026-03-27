import { Component, OnInit, signal, computed, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { EquipmentService } from '../../../core/services/equipment.service';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';
import { EquipmentResponse } from '../../../shared/models/equipment.model';
import { VenueResponse } from '../../../shared/models/venue.model';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';

/**
 * EquipmentListComponent shows all equipment for the org in a filterable table.
 * Admins can filter by venue and category, and perform edit/delete actions.
 * Requires TOURNAMENT_ADMIN role — enforced by the route guard.
 */
@Component({
  selector: 'app-equipment-list',
  standalone: false,
  templateUrl: './equipment-list.html',
  styleUrl: './equipment-list.scss',
})
export class EquipmentList implements OnInit {
  readonly equipment = signal<EquipmentResponse[]>([]);
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);
  readonly filterVenueId = signal<number | null>(null);
  readonly filterCategory = signal<string | null>(null);

  readonly filtered = computed(() => {
    return this.equipment().filter((e) => {
      const venueMatch = !this.filterVenueId() || e.venueId === this.filterVenueId();
      const categoryMatch = !this.filterCategory() || e.category === this.filterCategory();
      return venueMatch && categoryMatch;
    });
  });

  readonly categories = computed(() => {
    const cats = new Set(this.equipment().map((e) => e.category));
    return Array.from(cats).sort();
  });

  readonly displayedColumns = ['name', 'category', 'venue', 'serial', 'availability', 'actions'];

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly equipmentService: EquipmentService,
    private readonly venueService: VenueService,
    private readonly router: Router,
    private readonly tenantService: TenantService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  /**
   * Loads venues and all equipment in parallel.
   */
  load(): void {
    this.loading.set(true);
    forkJoin({
      venues: this.venueService.getAll(),
      equipment: this.equipmentService.getAll(),
    }).subscribe({
      next: ({ venues, equipment }) => {
        this.venues.set(venues);
        this.equipment.set(equipment);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load equipment.', 'OK', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  /**
   * Navigates to the equipment creation form.
   */
  createEquipment(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'equipment', 'new']);
  }

  /**
   * Navigates to the edit form for a specific piece of equipment.
   * @param id Equipment primary key.
   */
  editEquipment(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'equipment', id, 'edit']);
  }

  /**
   * Opens a confirm dialog then deletes the equipment on confirmation.
   * @param id Equipment primary key.
   * @param name Equipment display name (shown in the dialog).
   */
  deleteEquipment(id: number, name: string): void {
    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Delete Equipment',
          message: `Delete "${name}"? This action cannot be undone.`,
          confirmLabel: 'Delete',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.equipmentService.delete(id).subscribe({
          next: () => {
            this.snackBar.open('Equipment deleted.', 'OK', { duration: 3000 });
            this.load();
          },
          error: () => this.snackBar.open('Failed to delete equipment.', 'OK', { duration: 4000 }),
        });
      });
  }

  /**
   * Updates the venue filter signal.
   * @param venueId The venue to filter by, or null to show all.
   */
  setVenueFilter(venueId: number | null): void {
    this.filterVenueId.set(venueId);
  }

  /**
   * Updates the category filter signal.
   * @param category The category to filter by, or null to show all.
   */
  setCategoryFilter(category: string | null): void {
    this.filterCategory.set(category);
  }
}
