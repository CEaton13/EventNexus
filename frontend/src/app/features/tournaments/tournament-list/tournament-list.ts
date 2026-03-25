import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { PublicTournamentService } from '../../../core/services/public-tournament.service';
import { GenreService } from '../../../core/services/genre.service';
import { TenantService } from '../../../core/services/tenant.service';
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

  /** True when accessed via the unauthenticated public /tournaments route. */
  get isPublicContext(): boolean {
    return !this.tenantService.currentOrgSlug();
  }

  constructor(
    private readonly tournamentService: TournamentService,
    private readonly publicTournamentService: PublicTournamentService,
    private readonly genreService: GenreService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.genreService.getAll().subscribe((g) => this.genres.set(g));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const obs = this.isPublicContext
      ? this.publicTournamentService.getAll(this.page, this.pageSize)
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
