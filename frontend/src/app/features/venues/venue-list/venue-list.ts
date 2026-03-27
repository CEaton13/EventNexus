import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { VenueService } from '../../../core/services/venue.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { VenueResponse } from '../../../shared/models/venue.model';

/**
 * VenueListComponent shows all venues for the active organization.
 * Admins see controls to create, edit, and delete venues.
 */
@Component({
  selector: 'app-venue-list',
  standalone: false,
  templateUrl: './venue-list.html',
  styleUrl: './venue-list.scss',
})
export class VenueList implements OnInit {
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);

  constructor(
    private readonly venueService: VenueService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.venueService.getAll().subscribe({
      next: venues => {
        this.venues.set(venues);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openVenue(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'venues', id]);
  }

  createVenue(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'venues', 'new']);
  }
}
