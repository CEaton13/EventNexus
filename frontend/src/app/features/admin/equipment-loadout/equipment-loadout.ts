import { Component, OnInit, signal, computed, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { EquipmentService } from '../../../core/services/equipment.service';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import { EquipmentResponse, LoadoutResponse } from '../../../shared/models/equipment.model';
import { RegistrationResponse } from '../../../shared/models/tournament.model';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';

/** Groups loadouts by team for display in the template. */
export interface TeamLoadoutGroup {
  teamId: number;
  teamName: string;
  loadouts: LoadoutResponse[];
}

/**
 * EquipmentLoadoutComponent manages equipment assignments (loadouts) for a tournament.
 * Admins can assign available equipment to registered teams and return assigned equipment.
 * Requires TOURNAMENT_ADMIN role — enforced by the route guard.
 */
@Component({
  selector: 'app-equipment-loadout',
  standalone: false,
  templateUrl: './equipment-loadout.html',
  styleUrl: './equipment-loadout.scss',
})
export class EquipmentLoadout implements OnInit {
  readonly loadouts = signal<LoadoutResponse[]>([]);
  readonly availableEquipment = signal<EquipmentResponse[]>([]);
  readonly registrations = signal<RegistrationResponse[]>([]);
  readonly loading = signal(false);
  readonly assigning = signal(false);

  readonly selectedTeamId = signal<number | null>(null);
  readonly selectedEquipmentId = signal<number | null>(null);

  /** Groups current loadouts by team for display. */
  readonly loadoutsByTeam = computed((): TeamLoadoutGroup[] => {
    const groups = new Map<number, TeamLoadoutGroup>();
    for (const loadout of this.loadouts()) {
      const existing = groups.get(loadout.teamId);
      if (existing) {
        groups.set(loadout.teamId, {
          ...existing,
          loadouts: [...existing.loadouts, loadout],
        });
      } else {
        groups.set(loadout.teamId, {
          teamId: loadout.teamId,
          teamName: loadout.teamName,
          loadouts: [loadout],
        });
      }
    }
    return Array.from(groups.values());
  });

  /** Filters equipment to only show currently available items. */
  readonly unassignedEquipment = computed(() =>
    this.availableEquipment().filter((e) => e.isAvailable),
  );

  /** Returns only APPROVED registrations for the assign dropdown. */
  readonly approvedTeams = computed(() =>
    this.registrations().filter((r) => r.registrationStatus === 'APPROVED'),
  );

  readonly canAssign = computed(
    () => this.selectedTeamId() !== null && this.selectedEquipmentId() !== null,
  );

  private readonly destroyRef = inject(DestroyRef);
  private tournamentId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly equipmentService: EquipmentService,
    private readonly tournamentService: TournamentService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('tournamentId'));
    this.load();
  }

  /**
   * Loads loadouts, all equipment, and registered teams in parallel.
   */
  load(): void {
    this.loading.set(true);
    forkJoin({
      loadouts: this.equipmentService.getLoadouts(this.tournamentId),
      equipment: this.equipmentService.getAll(),
      registrations: this.tournamentService.getRegisteredTeams(this.tournamentId),
    }).subscribe({
      next: ({ loadouts, equipment, registrations }) => {
        this.loadouts.set(loadouts);
        this.availableEquipment.set(equipment);
        this.registrations.set(registrations);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load loadout data.', 'OK', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  /**
   * Assigns the selected equipment to the selected team.
   */
  assignLoadout(): void {
    const teamId = this.selectedTeamId();
    const equipmentId = this.selectedEquipmentId();
    if (!teamId || !equipmentId) return;

    this.assigning.set(true);
    this.equipmentService.assignLoadout(this.tournamentId, { teamId, equipmentId }).subscribe({
      next: () => {
        this.snackBar.open('Equipment assigned.', 'OK', { duration: 3000 });
        this.selectedTeamId.set(null);
        this.selectedEquipmentId.set(null);
        this.assigning.set(false);
        this.load();
      },
      error: () => {
        this.assigning.set(false);
        this.snackBar.open('Failed to assign equipment.', 'OK', { duration: 4000 });
      },
    });
  }

  /**
   * Opens a confirm dialog then returns (removes) the loadout assignment.
   * @param loadoutId Loadout primary key.
   * @param equipmentName Equipment display name (shown in the dialog).
   */
  returnLoadout(loadoutId: number, equipmentName: string): void {
    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Return Equipment',
          message: `Mark "${equipmentName}" as returned? The equipment will become available again.`,
          confirmLabel: 'Return',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.equipmentService.removeLoadout(this.tournamentId, loadoutId).subscribe({
          next: () => {
            this.snackBar.open('Equipment returned.', 'OK', { duration: 3000 });
            this.load();
          },
          error: () => this.snackBar.open('Failed to return equipment.', 'OK', { duration: 4000 }),
        });
      });
  }

  /**
   * Navigates back to the tournament detail page.
   */
  back(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments', this.tournamentId]);
  }
}
