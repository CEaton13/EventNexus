import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { TenantService } from '../../../core/services/tenant.service';
import { TournamentSummary } from '../../../shared/models/tournament.model';

/**
 * RegistrationsHub lists all tournaments so admins can navigate to each
 * tournament's registration manager. Tournaments in REGISTRATION_OPEN or
 * REGISTRATION_CLOSED are highlighted as actionable.
 */
@Component({
  selector: 'app-registrations-hub',
  standalone: false,
  templateUrl: './registrations-hub.html',
  styleUrl: './registrations-hub.scss',
})
export class RegistrationsHub implements OnInit {
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
   * Returns true when the tournament's status makes registrations actionable.
   * @param status Tournament lifecycle status.
   */
  isActionable(status: string): boolean {
    return status === 'REGISTRATION_OPEN' || status === 'REGISTRATION_CLOSED';
  }

  /**
   * Navigates to the registration manager for a specific tournament.
   * @param id Tournament primary key.
   */
  manage(id: number): void {
    this.router.navigate([
      this.tenantService.currentOrgSlug(),
      'admin',
      'tournaments',
      id,
      'registrations',
    ]);
  }
}
