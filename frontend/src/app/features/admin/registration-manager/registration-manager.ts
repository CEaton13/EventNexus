import { Component, OnInit, signal, computed, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import {
  TournamentDetail as TournamentDetailModel,
  RegistrationResponse,
} from '../../../shared/models/tournament.model';

/**
 * RegistrationManager provides admins with a dedicated interface to manage
 * tournament team registrations. Features include tabbed views by status,
 * bulk approve/reject operations, checkbox selection, and a capacity warning bar.
 */
@Component({
  selector: 'app-registration-manager',
  standalone: false,
  templateUrl: './registration-manager.html',
  styleUrl: './registration-manager.scss',
})
export class RegistrationManager implements OnInit {
  readonly tournament = signal<TournamentDetailModel | null>(null);
  readonly registrations = signal<RegistrationResponse[]>([]);
  readonly loading = signal(false);
  readonly selectedTeamIds = signal<Set<number>>(new Set());

  readonly pendingRegistrations = computed(() =>
    this.registrations().filter((r) => r.registrationStatus === 'PENDING'),
  );

  readonly approvedRegistrations = computed(() =>
    this.registrations().filter((r) => r.registrationStatus === 'APPROVED'),
  );

  readonly rejectedRegistrations = computed(() =>
    this.registrations().filter((r) => r.registrationStatus === 'REJECTED'),
  );

  readonly approvedCount = computed(() => this.approvedRegistrations().length);

  readonly capacityPct = computed(() => {
    const t = this.tournament();
    if (!t || t.maxTeams === 0) return 0;
    return (this.approvedCount() / t.maxTeams) * 100;
  });

  readonly atCapacity = computed(() => {
    const t = this.tournament();
    return !!t && this.approvedCount() >= t.maxTeams;
  });

  private readonly destroyRef = inject(DestroyRef);
  private tournamentId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly tournamentService: TournamentService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  /**
   * Loads tournament detail and registrations in parallel.
   */
  load(): void {
    this.loading.set(true);
    forkJoin({
      tournament: this.tournamentService.getById(this.tournamentId),
      registrations: this.tournamentService.getRegisteredTeams(this.tournamentId),
    }).subscribe({
      next: ({ tournament, registrations }) => {
        this.tournament.set(tournament);
        this.registrations.set(registrations);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load registrations.', 'OK', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  /**
   * Approves a single team's registration.
   * @param teamId The team's primary key.
   */
  approveTeam(teamId: number): void {
    this.tournamentService
      .updateRegistrationStatus(this.tournamentId, teamId, 'APPROVED')
      .subscribe({
        next: () => {
          this.snackBar.open('Team approved.', 'OK', { duration: 3000 });
          this.load();
        },
        error: () => {
          this.snackBar.open('Failed to approve team.', 'OK', { duration: 4000 });
        },
      });
  }

  /**
   * Opens a confirm dialog before rejecting a team's registration.
   * @param teamId The team's primary key.
   * @param teamName The team's display name (shown in the dialog).
   */
  rejectTeam(teamId: number, teamName: string): void {
    const t = this.tournament();
    if (!t) return;

    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Reject Registration',
          message: `Reject ${teamName}'s registration for "${t.name}"? They will need to re-register if you change your mind.`,
          confirmLabel: 'Reject',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.tournamentService
          .updateRegistrationStatus(this.tournamentId, teamId, 'REJECTED')
          .subscribe({
            next: () => {
              this.snackBar.open(`${teamName}'s registration rejected.`, 'OK', { duration: 3000 });
              this.load();
            },
            error: () => {
              this.snackBar.open('Failed to reject team.', 'OK', { duration: 4000 });
            },
          });
      });
  }

  /**
   * Opens a confirm dialog and approves all currently pending teams at once.
   */
  approveAll(): void {
    const pending = this.pendingRegistrations();
    if (pending.length === 0) return;

    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Approve All Pending',
          message: `Approve all ${pending.length} pending team${pending.length === 1 ? '' : 's'}?`,
          confirmLabel: 'Approve All',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        const approvals = pending.map((r) =>
          this.tournamentService.updateRegistrationStatus(this.tournamentId, r.teamId, 'APPROVED'),
        );
        forkJoin(approvals).subscribe({
          next: () => {
            this.snackBar.open(
              `${pending.length} team${pending.length === 1 ? '' : 's'} approved.`,
              'OK',
              {
                duration: 3000,
              },
            );
            this.load();
          },
          error: () => {
            this.snackBar.open('Some approvals failed. Please try again.', 'OK', {
              duration: 4000,
            });
            this.load();
          },
        });
      });
  }

  /**
   * Adds or removes a team ID from the selection set (immutable update).
   * @param teamId The team's primary key.
   */
  toggleSelection(teamId: number): void {
    this.selectedTeamIds.update((set) => {
      const next = new Set(set);
      if (next.has(teamId)) {
        next.delete(teamId);
      } else {
        next.add(teamId);
      }
      return next;
    });
  }

  /**
   * Returns whether a given team is currently selected.
   * @param teamId The team's primary key.
   */
  isSelected(teamId: number): boolean {
    return this.selectedTeamIds().has(teamId);
  }

  /**
   * Opens a confirm dialog then approves all currently selected teams.
   */
  bulkApprove(): void {
    const ids = Array.from(this.selectedTeamIds());
    if (ids.length === 0) return;

    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Approve Selected Teams',
          message: `Approve ${ids.length} selected team${ids.length === 1 ? '' : 's'}?`,
          confirmLabel: 'Approve All',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        const approvals = ids.map((id) =>
          this.tournamentService.updateRegistrationStatus(this.tournamentId, id, 'APPROVED'),
        );
        forkJoin(approvals).subscribe({
          next: () => {
            this.snackBar.open(`${ids.length} team${ids.length === 1 ? '' : 's'} approved.`, 'OK', {
              duration: 3000,
            });
            this.clearSelection();
            this.load();
          },
          error: () => {
            this.snackBar.open('Some approvals failed. Please try again.', 'OK', {
              duration: 4000,
            });
            this.load();
          },
        });
      });
  }

  /**
   * Opens a confirm dialog then rejects all currently selected teams.
   */
  bulkReject(): void {
    const ids = Array.from(this.selectedTeamIds());
    if (ids.length === 0) return;

    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Reject Selected Teams',
          message: `Reject ${ids.length} selected team${ids.length === 1 ? '' : 's'}?`,
          confirmLabel: 'Reject All',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        const rejections = ids.map((id) =>
          this.tournamentService.updateRegistrationStatus(this.tournamentId, id, 'REJECTED'),
        );
        forkJoin(rejections).subscribe({
          next: () => {
            this.snackBar.open(`${ids.length} team${ids.length === 1 ? '' : 's'} rejected.`, 'OK', {
              duration: 3000,
            });
            this.clearSelection();
            this.load();
          },
          error: () => {
            this.snackBar.open('Some rejections failed. Please try again.', 'OK', {
              duration: 4000,
            });
            this.load();
          },
        });
      });
  }

  /**
   * Clears the selection set (immutable reset).
   */
  clearSelection(): void {
    this.selectedTeamIds.set(new Set());
  }

  /**
   * Navigates back to the registrations hub.
   */
  back(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'registrations']);
  }
}
