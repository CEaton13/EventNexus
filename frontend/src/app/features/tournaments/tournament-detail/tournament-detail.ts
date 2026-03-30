import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { MatchDetailDialog } from '../../tournament-hub/match-detail-dialog/match-detail-dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TournamentService } from '../../../core/services/tournament.service';
import { TournamentFavoriteService } from '../../../core/services/tournament-favorite.service';
import { MatchService } from '../../../core/services/match.service';
import { VenueService } from '../../../core/services/venue.service';
import { ThemeService } from '../../../core/services/theme';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import {
  TournamentDetail as TournamentDetailModel,
  StandingsResponse,
  RegistrationResponse,
} from '../../../shared/models/tournament.model';
import { BracketResponse } from '../../../shared/models/match.model';
import { VenueResponse } from '../../../shared/models/venue.model';

/**
 * TournamentDetailComponent loads and displays full tournament info,
 * standings, bracket, and admin controls. Applies the genre theme on init
 * and resets it on destroy.
 */
@Component({
  selector: 'app-tournament-detail',
  standalone: false,
  templateUrl: './tournament-detail.html',
  styleUrl: './tournament-detail.scss',
})
export class TournamentDetail implements OnInit, OnDestroy {
  readonly tournament = signal<TournamentDetailModel | null>(null);
  readonly standings = signal<StandingsResponse[]>([]);
  readonly bracket = signal<BracketResponse | null>(null);
  readonly registrations = signal<RegistrationResponse[]>([]);
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);
  readonly favorited = signal(false);
  readonly favoriteLoading = signal(false);

  // Match scheduling form state
  scheduleMatchId: number | null = null;
  scheduleVenueId: number | null = null;
  scheduleTime = '';
  resultMatchId: number | null = null;
  resultWinnerId: number | null = null;

  readonly statusTransitions: Record<string, string> = {
    DRAFT: 'REGISTRATION_OPEN',
    REGISTRATION_OPEN: 'REGISTRATION_CLOSED',
    REGISTRATION_CLOSED: 'IN_PROGRESS',
    IN_PROGRESS: 'COMPLETED',
    COMPLETED: 'ARCHIVED',
  };

  private tournamentId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    readonly tournamentService: TournamentService,
    private readonly tournamentFavoriteService: TournamentFavoriteService,
    private readonly matchService: MatchService,
    private readonly venueService: VenueService,
    private readonly themeService: ThemeService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
    if (this.authService.isAuthenticated()) {
      this.loadFavoriteStatus();
    }
    if (this.authService.isAdmin()) {
      this.venueService.getAll().subscribe((v) => this.venues.set(v));
    }
  }

  ngOnDestroy(): void {
    this.themeService.resetTheme();
  }

  load(): void {
    this.loading.set(true);
    this.tournamentService.getById(this.tournamentId).subscribe({
      next: (t) => {
        this.tournament.set(t);
        this.themeService.applyGenre(t.gameGenre);
        this.loading.set(false);
        this.loadSubResources();
      },
      error: () => this.loading.set(false),
    });
  }

  private loadSubResources(): void {
    this.tournamentService.getStandings(this.tournamentId).subscribe((s) => this.standings.set(s));
    this.tournamentService.getBracket(this.tournamentId).subscribe((b) => this.bracket.set(b));
    this.tournamentService
      .getRegisteredTeams(this.tournamentId)
      .subscribe((r) => this.registrations.set(r));
  }

  private loadFavoriteStatus(): void {
    this.tournamentFavoriteService.isFavorited(this.tournamentId).subscribe({
      next: ({ favorited }) => this.favorited.set(favorited),
    });
  }

  toggleFavorite(): void {
    if (this.favoriteLoading()) return;
    this.favoriteLoading.set(true);
    const action = this.favorited()
      ? this.tournamentFavoriteService.unfavorite(this.tournamentId)
      : this.tournamentFavoriteService.favorite(this.tournamentId);

    action.subscribe({
      next: () => {
        this.favorited.set(!this.favorited());
        this.favoriteLoading.set(false);
        this.snackBar.open(
          this.favorited() ? 'Added to favorites' : 'Removed from favorites',
          'OK',
          { duration: 2000 },
        );
      },
      error: () => this.favoriteLoading.set(false),
    });
  }

  advanceStatus(): void {
    const t = this.tournament();
    if (!t) return;
    const next = this.statusTransitions[t.status];
    if (!next) return;

    const message =
      next === 'IN_PROGRESS'
        ? `Move "${t.name}" to IN_PROGRESS? This will lock registrations and generate the bracket.`
        : `Advance tournament to ${next}?`;

    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Advance Tournament Status',
          message,
          confirmLabel: 'Confirm',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.tournamentService.updateStatus(this.tournamentId, next as any).subscribe({
          next: (updated) => {
            this.tournament.set(updated);
            this.snackBar.open(`Status advanced to ${updated.status}`, 'OK', { duration: 3000 });
          },
        });
      });
  }

  scheduleMatch(): void {
    if (!this.scheduleMatchId || !this.scheduleVenueId || !this.scheduleTime) return;
    this.matchService
      .schedule(this.scheduleMatchId, this.scheduleTime, this.scheduleVenueId)
      .subscribe({
        next: () => {
          this.snackBar.open('Match scheduled', 'OK', { duration: 3000 });
          this.scheduleMatchId = null;
          this.scheduleVenueId = null;
          this.scheduleTime = '';
          this.tournamentService
            .getBracket(this.tournamentId)
            .subscribe((b) => this.bracket.set(b));
        },
      });
  }

  recordResult(): void {
    if (!this.resultMatchId || !this.resultWinnerId) return;
    this.matchService.recordResult(this.resultMatchId, this.resultWinnerId).subscribe({
      next: () => {
        this.snackBar.open('Result recorded', 'OK', { duration: 3000 });
        this.resultMatchId = null;
        this.resultWinnerId = null;
        this.loadSubResources();
      },
    });
  }

  nextStatus(): string | null {
    const t = this.tournament();
    return t ? (this.statusTransitions[t.status] ?? null) : null;
  }

  /**
   * Opens a confirmation dialog before rejecting a team's registration.
   * Calling the service directly from the template is unsafe — accidental
   * clicks can immediately reject a team with no way to undo.
   */
  rejectRegistration(teamId: number, teamName: string): void {
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
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.tournamentService.updateRegistrationStatus(t.id, teamId, 'REJECTED').subscribe({
          next: () => {
            this.snackBar.open(`${teamName} registration rejected.`, 'OK', { duration: 3000 });
            this.loadSubResources();
          },
        });
      });
  }

  /**
   * Approves a team's registration for this tournament.
   * @param teamId The team to approve.
   */
  approveRegistration(teamId: number): void {
    const t = this.tournament();
    if (!t) return;
    this.tournamentService.updateRegistrationStatus(t.id, teamId, 'APPROVED').subscribe({
      next: () => {
        this.snackBar.open('Registration approved.', 'OK', { duration: 3000 });
        this.loadSubResources();
      },
      error: () => this.snackBar.open('Failed to approve registration.', 'OK', { duration: 4000 }),
    });
  }

  /**
   * Opens the MatchDetailDialog for the given match ID.
   * @param matchId Primary key of the match to display.
   */
  openMatchDetail(matchId: number): void {
    this.dialog.open(MatchDetailDialog, {
      data: { matchId },
      panelClass: 'dark-dialog',
      width: '420px',
    });
  }

  backToList(): void {
    const slug = this.tenantService.currentOrgSlug();
    this.router.navigate([slug, 'tournaments']);
  }

  /**
   * Navigates to the dedicated registration management page for this tournament.
   * Only accessible to admins.
   */
  manageRegistrations(): void {
    this.router.navigate([
      this.tenantService.currentOrgSlug(),
      'admin',
      'tournaments',
      this.tournamentId,
      'registrations',
    ]);
  }
}
