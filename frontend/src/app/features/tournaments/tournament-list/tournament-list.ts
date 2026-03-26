import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { PublicTournamentService } from '../../../core/services/public-tournament.service';
import { TeamService } from '../../../core/services/team.service';
import { GenreService } from '../../../core/services/genre.service';
import { TenantService } from '../../../core/services/tenant.service';
import { AuthService } from '../../../core/services/auth';
import { TournamentSummary, TournamentStatus } from '../../../shared/models/tournament.model';
import { GameGenreResponse } from '../../../core/services/theme';

/**
 * TournamentListComponent displays a paginated grid of tournament cards.
 *
 * When accessed via the public /tournaments route (no org context) it uses
 * PublicTournamentService and navigates to the public hub at /t/:id.
 * When accessed via /:orgSlug/tournaments it uses TournamentService and
 * navigates to the org-scoped detail at /:orgSlug/tournaments/:id.
 */
@Component({
  selector: 'app-tournament-list',
  standalone: false,
  templateUrl: './tournament-list.html',
  styleUrl: './tournament-list.scss',
})
export class TournamentList implements OnInit {
  readonly tournaments = signal<TournamentSummary[]>([]);
  readonly genres = signal<GameGenreResponse[]>([]);
  readonly loading = signal(false);
  readonly totalElements = signal(0);

  page = 0;
  readonly pageSize = 20;
  selectedStatus = '';
  selectedGenreId: number | undefined;

  readonly statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'DRAFT', label: 'Draft' },
    { value: 'REGISTRATION_OPEN', label: 'Registration Open' },
    { value: 'REGISTRATION_CLOSED', label: 'Registration Closed' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'ARCHIVED', label: 'Archived' },
  ];

  /**
   * True when accessed via the public /tournaments route (no orgSlug in URL).
   * Uses the route param tree rather than TenantService so that a logged-in
   * manager visiting /tournaments still sees all tournaments.
   */
  get isPublicContext(): boolean {
    return !this.route.snapshot.pathFromRoot.some((s) => s.params['orgSlug']);
  }

  /** Team ID for the currently logged-in manager, null otherwise. */
  private managerTeamId: number | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly tournamentService: TournamentService,
    private readonly publicTournamentService: PublicTournamentService,
    private readonly teamService: TeamService,
    private readonly genreService: GenreService,
    private readonly tenantService: TenantService,
    readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.genreService.getAll().subscribe((g) => this.genres.set(g));

    // For team managers in the org context, load only their team's tournaments.
    if (!this.isPublicContext && this.authService.isTeamManager()) {
      this.teamService.getMyTeams().subscribe({
        next: (teams) => {
          this.managerTeamId = teams.length > 0 ? teams[0].id : null;
          this.load();
        },
        error: () => this.load(),
      });
    } else {
      this.load();
    }
  }

  load(): void {
    this.loading.set(true);

    // Team managers see only their team's tournaments.
    if (this.managerTeamId !== null) {
      this.teamService.getTournaments(this.managerTeamId).subscribe({
        next: (list) => {
          this.tournaments.set(list);
          this.totalElements.set(list.length);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
      return;
    }

    const obs = this.isPublicContext
      ? this.publicTournamentService.getAll(
          this.page,
          this.pageSize,
          this.selectedStatus || undefined,
          this.selectedGenreId,
        )
      : this.tournamentService.getAll(
          this.page,
          this.pageSize,
          this.selectedStatus || undefined,
          this.selectedGenreId,
        );

    obs.subscribe({
      next: (page) => {
        this.tournaments.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onStatusChange(status: string): void {
    this.selectedStatus = status;
    this.page = 0;
    this.load();
  }

  onGenreChange(genreId: number | undefined): void {
    this.selectedGenreId = genreId;
    this.page = 0;
    this.load();
  }

  onPageChange(newPage: number): void {
    this.page = newPage;
    this.load();
  }

  openTournament(id: number): void {
    if (this.isPublicContext) {
      this.router.navigate(['/t', id]);
    } else {
      this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments', id]);
    }
  }

  statusColor(status: TournamentStatus): string {
    const map: Record<TournamentStatus, string> = {
      DRAFT: 'default',
      REGISTRATION_OPEN: 'primary',
      REGISTRATION_CLOSED: 'accent',
      IN_PROGRESS: 'warn',
      COMPLETED: '',
      ARCHIVED: '',
    };
    return map[status] ?? '';
  }
}
