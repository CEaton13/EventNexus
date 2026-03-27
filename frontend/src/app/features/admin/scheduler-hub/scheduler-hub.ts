import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import { TournamentSummary } from '../../../shared/models/tournament.model';

/**
 * SchedulerHub lists all tournaments so admins can navigate to each
 * tournament's match scheduler. Tournaments in REGISTRATION_CLOSED or
 * IN_PROGRESS are highlighted as actionable.
 */
@Component({
  selector: 'app-scheduler-hub',
  standalone: false,
  templateUrl: './scheduler-hub.html',
  styleUrl: './scheduler-hub.scss',
})
export class SchedulerHub implements OnInit {
  readonly tournaments = signal<TournamentSummary[]>([]);
  readonly loading = signal(false);

  constructor(
    private readonly tournamentService: TournamentService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.tournamentService.getAll(0, 50).subscribe({
      next: (page) => {
        this.tournaments.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  /**
   * Returns true when the tournament's status makes scheduling actionable.
   * @param status Tournament lifecycle status.
   */
  isActionable(status: string): boolean {
    return status === 'REGISTRATION_CLOSED' || status === 'IN_PROGRESS';
  }

  /**
   * Navigates to the match scheduler for a specific tournament.
   * @param id Tournament primary key.
   */
  schedule(id: number): void {
    this.router.navigate([
      this.tenantService.currentOrgSlug(),
      'admin',
      'tournaments',
      id,
      'schedule',
    ]);
  }
}
