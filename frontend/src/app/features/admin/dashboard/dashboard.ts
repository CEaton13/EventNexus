import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import {
  DashboardSummary,
  TournamentStatus,
  TournamentSummary,
} from '../../../shared/models/tournament.model';

/**
 * AdminDashboardComponent shows aggregate org metrics at the top and a
 * recent-tournament list below for quick status management.
 */
@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  readonly summary = signal<DashboardSummary | null>(null);
  readonly tournaments = signal<TournamentSummary[]>([]);
  readonly loading = signal(false);

  readonly statusTransitions: Record<string, TournamentStatus> = {
    DRAFT: 'REGISTRATION_OPEN',
    REGISTRATION_OPEN: 'REGISTRATION_CLOSED',
    REGISTRATION_CLOSED: 'IN_PROGRESS',
    IN_PROGRESS: 'COMPLETED',
    COMPLETED: 'ARCHIVED',
  };

  readonly statusOrder: TournamentStatus[] = [
    'DRAFT',
    'REGISTRATION_OPEN',
    'REGISTRATION_CLOSED',
    'IN_PROGRESS',
    'COMPLETED',
    'ARCHIVED',
  ];

  constructor(
    private readonly dialog: MatDialog,
    private readonly tournamentService: TournamentService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loading.set(true);

    this.tournamentService.getDashboard().subscribe({
      next: (data) => {
        this.summary.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.tournamentService.getAll(0, 10).subscribe({
      next: (page) => this.tournaments.set(page.content),
    });
  }

  statusCount(status: TournamentStatus): number {
    return this.summary()?.tournamentsByStatus[status] ?? 0;
  }

  nextStatus(status: string): TournamentStatus | null {
    return this.statusTransitions[status] ?? null;
  }

  advance(t: TournamentSummary): void {
    const next = this.nextStatus(t.status);
    if (!next) return;
    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Advance Tournament Status',
          message: `Advance "${t.name}" to ${next}?`,
          confirmLabel: 'Confirm',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.tournamentService.updateStatus(t.id, next).subscribe({
          next: (updated) => {
            this.tournaments.update((list) =>
              list.map((item) =>
                item.id === updated.id ? { ...item, status: updated.status } : item,
              ),
            );
            // Refresh metrics after a status change.
            this.tournamentService.getDashboard().subscribe({
              next: (data) => this.summary.set(data),
            });
          },
        });
      });
  }

  viewAllTournaments(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments']);
  }

  createTournament(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'tournaments', 'new']);
  }

  viewTournament(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments', id]);
  }
}
