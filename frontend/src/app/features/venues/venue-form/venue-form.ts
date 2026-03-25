import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * VenueFormComponent handles both creating and editing a venue.
 * Presence of an `:id` route param determines create vs edit mode.
 * Requires TOURNAMENT_ADMIN role — enforced by the route guard.
 */
@Component({
  selector: 'app-venue-form',
  standalone: false,
  templateUrl: './venue-form.html',
  styleUrl: './venue-form.scss',
})
export class VenueForm implements OnInit {
  form!: FormGroup;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly isEdit = signal(false);

  private venueId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly venueService: VenueService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      location: ['', [Validators.required, Validators.maxLength(500)]],
      stationCount: [1, [Validators.required, Validators.min(1)]],
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.venueId = Number(id);
      this.isEdit.set(true);
      this.loadVenue();
    }
  }

  private loadVenue(): void {
    this.loading.set(true);
    this.venueService.getById(this.venueId!).subscribe({
      next: venue => {
        this.form.patchValue({
          name: venue.name,
          location: venue.location,
          stationCount: venue.stationCount,
        });
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Venue not found', 'OK', { duration: 3000 });
        this.cancel();
      },
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const payload = this.form.value;
    const request$ = this.isEdit()
      ? this.venueService.update(this.venueId!, payload)
      : this.venueService.create(payload);

    request$.subscribe({
      next: venue => {
        this.snackBar.open(
          this.isEdit() ? 'Venue updated' : 'Venue created',
          'OK',
          { duration: 3000 },
        );
        this.router.navigate([this.tenantService.currentOrgSlug(), 'venues', venue.id]);
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Failed to save venue', 'OK', { duration: 3000 });
      },
    });
  }

  cancel(): void {
    if (this.venueId) {
      this.router.navigate([this.tenantService.currentOrgSlug(), 'venues', this.venueId]);
    } else {
      this.router.navigate([this.tenantService.currentOrgSlug(), 'venues']);
    }
  }
}
