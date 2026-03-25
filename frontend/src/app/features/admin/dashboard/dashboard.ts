import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import { TournamentSummary } from '../../../shared/models/tournament.model';

/**
 * AdminDashboardComponent provides an overview of recent tournaments
 * with quick status controls and a link to create a new tournament.
 */
@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  readonly tournaments = signal<TournamentSummary[]>([]);
  readonly loading = signal(false);

  readonly statusTransitions: Record<string, string> = {
    DRAFT: 'REGISTRATION_OPEN',
    REGISTRATION_OPEN: 'REGISTRATION_CLOSED',
    REGISTRATION_CLOSED: 'IN_PROGRESS',
    IN_PROGRESS: 'COMPLETED',
    COMPLETED: 'ARCHIVED',
  };

  constructor(
    private readonly tournamentService: TournamentService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.tournamentService.getAll(0, 10).subscribe({
      next: page => {
        this.tournaments.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  nextStatus(status: string): string | null {
    return this.statusTransitions[status] ?? null;
  }

  advance(t: TournamentSummary): void {
    const next = this.nextStatus(t.status);
    if (!next) return;
    this.tournamentService.updateStatus(t.id, next as any).subscribe({
      next: updated => {
        this.tournaments.update(list =>
          list.map(item => item.id === updated.id ? { ...item, status: updated.status } : item)
        );
      },
    });
  }

  createTournament(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'tournaments', 'new']);
  }

  viewTournament(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments', id]);
  }
}
