import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { EquipmentService } from '../../../core/services/equipment.service';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';
import { VenueResponse } from '../../../shared/models/venue.model';

/**
 * EquipmentFormComponent handles both creating and editing a piece of equipment.
 * Presence of an `:id` route param determines create vs edit mode.
 * Requires TOURNAMENT_ADMIN role — enforced by the route guard.
 */
@Component({
  selector: 'app-equipment-form',
  standalone: false,
  templateUrl: './equipment-form.html',
  styleUrl: './equipment-form.scss',
})
export class EquipmentForm implements OnInit {
  form!: FormGroup;
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly isEdit = signal(false);

  private equipmentId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly equipmentService: EquipmentService,
    private readonly venueService: VenueService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      category: ['', [Validators.required, Validators.maxLength(100)]],
      serialNumber: ['', [Validators.maxLength(100)]],
      venueId: [null, [Validators.required]],
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.equipmentId = Number(id);
      this.isEdit.set(true);
    }

    this.loadVenues();
  }

  /**
   * Loads the venue list and (if editing) the existing equipment data in parallel.
   */
  private loadVenues(): void {
    this.loading.set(true);

    if (this.isEdit()) {
      forkJoin({
        venues: this.venueService.getAll(),
        equipment: this.equipmentService.getById(this.equipmentId!),
      }).subscribe({
        next: ({ venues, equipment }) => {
          this.venues.set(venues);
          this.form.patchValue({
            name: equipment.name,
            category: equipment.category,
            serialNumber: equipment.serialNumber ?? '',
            venueId: equipment.venueId,
          });
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Failed to load equipment.', 'OK', { duration: 3000 });
          this.cancel();
        },
      });
    } else {
      this.venueService.getAll().subscribe({
        next: (venues) => {
          this.venues.set(venues);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Failed to load venues.', 'OK', { duration: 3000 });
          this.loading.set(false);
        },
      });
    }
  }

  /**
   * Submits the form to create or update equipment.
   */
  save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);

    const value = this.form.value;
    const payload = {
      name: value.name,
      category: value.category,
      venueId: value.venueId,
      ...(value.serialNumber ? { serialNumber: value.serialNumber } : {}),
    };

    const request$ = this.isEdit()
      ? this.equipmentService.update(this.equipmentId!, payload)
      : this.equipmentService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open(this.isEdit() ? 'Equipment updated.' : 'Equipment created.', 'OK', {
          duration: 3000,
        });
        this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'equipment']);
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Failed to save equipment.', 'OK', { duration: 3000 });
      },
    });
  }

  /**
   * Navigates back to the equipment list without saving.
   */
  cancel(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'equipment']);
  }
}
